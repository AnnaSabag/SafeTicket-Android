package com.safeticket.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.safeticket.R;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddTicketFragment extends Fragment {

    private View layoutStepID, layoutStepSelfie, layoutStepDetails; // Declare the identify to 3 steps
    private ImageView ivIDCard, ivSelfie, ivTicketImage; // Declare the image views
    private LinearLayout layoutUploadPrompt; // Declare the layout for the upload prompt
    private EditText etEventName, etPrice, etOriginalPrice, etEventDate, etEventTime, etExactAddress, etQuantity; // Declare the edit texts, input fields
    private AutoCompleteTextView etLocation, spCategory; // Declare the auto complete text views and the spinner to select the category and location
    private Button btnSaveTicket; // Declare the button to save the ticket
    private FirebaseFirestore db; // Declare the Firebase Firestore database
    private String currentUserId; // Declare the current user ID
    private String tempIdCardBase64 = "", tempSelfieBase64 = "", tempTicketBase64 = ""; // Declare the temporary base64 strings for the images
    private String editTicketId = null; // Declare the ticket ID if we try to edit

    private Uri photoUri; // Declare the URI for the photo
    private String userRegisteredID = ""; // Declare the user registered ID

    // Declare the arrays for the cities and categories
    private static final String[] ISRAEL_CITIES = {"תל אביב - יפו", "ירושלים", "חיפה", "ראשון לציון", "פתח תקווה", "אשדוד", "נתניה", "באר שבע", "בני ברק", "חולון", "רמת גן", "רחובות", "אשקלון", "בת ים", "בית שמש", "כפר סבא", "הרצליה", "חדרה", "מודיעין-מכבים-רעות", "רעננה", "רמלה", "רהט", "גבעתאיים", "הוד השרון", "נהריה", "קריית אתא", "קריית גת", "אילת", "עכו", "כרמיאל", "קריית מוצקין", "רמת השרון", "עפולה", "נס ציונה", "קריית ים", "קריית ביאליק", "אור יהודה", "מעלה אדומים", "צפת", "דימונה", "טבריה", "יבנה", "נתיבות", "קריית שמונה", "נשר", "קריית מלאכי", "מעלות-תרשיחא", "שדרות"};
    private static final String[] CATEGORIES = {"הופעות", "ספורט", "קולנוע", "מסיבות", "אחר"};

    // Declare the launcher for the camera permission
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) openCamera(); else Toast.makeText(getContext(), "נדרשת הרשאת מצלמה לאימות", Toast.LENGTH_SHORT).show(); // If the permission is not granted, show a toast message. if the permission is granted, open the camera.
    });
    // Declare the launcher for the camera
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == getActivity().RESULT_OK) { // If the result is OK from the camera (take photo), load the image
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), photoUri); // Get the bitmap from the URI
                Bitmap optimizedBitmap = scaleAndFixImage(bitmap); // Scale and fix the image to a smaller size to save space in the database and prevent crashing
                if (layoutStepID.getVisibility() == View.VISIBLE) processIDCard(optimizedBitmap); // If the layout is the ID card, process it, else process the selfie
                else processSelfie(optimizedBitmap);
            } catch (Exception e) { // If there is an error loading the image, show a toast message
                Log.e("CameraError", "Load failed", e);
                Toast.makeText(getContext(), "שגיאה בטעינת התמונה", Toast.LENGTH_SHORT).show();
            }
        }
    });

    // Declare the launcher for the image picker
    private final ActivityResultLauncher<String> ticketImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) { // If the URI is not null, load the image
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri); // Get the bitmap from the URI
                Bitmap smallBitmap = scaleToSize(bitmap, 800); // Scale the bitmap to a smaller size to save space in the database and prevent crashing
                ivTicketImage.setImageBitmap(smallBitmap); // Set the bitmap to the ImageView
                if (layoutUploadPrompt != null) layoutUploadPrompt.setVisibility(View.GONE); // Hide the upload prompt if picture was uploaded
                tempTicketBase64 = encodeToBase64(smallBitmap, 50); // Encode the bitmap to a base64 string in order to save it in the database
            } catch (IOException e) { Log.e("AddTicket", "Error picking image", e); } // If there is an error loading the image, show a toast message
        }
    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_ticket, container, false); // Return the inflated view
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) { // After the view is created, set up the views
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance(); // Initialize the Firebase Firestore database
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user ID from Firebase Auth

        initViews(view); // Initialize the views
        setupCategoryDropdown(); // Set up the category
        setupLocationAutoComplete(); // Set up the location auto complete
        fetchUserRegisteredData(); // Fetch the user registered data

        etEventDate.setOnClickListener(v -> showDatePicker()); // Set the click listeners for the date and open calender
        etEventTime.setOnClickListener(v -> showTimePicker()); // Set the click listeners for the time and open time picker
        view.findViewById(R.id.frameUploadTicket).setOnClickListener(v -> ticketImageLauncher.launch("image/*")); // Set the click listener for the ticket image
        view.findViewById(R.id.btnVerifyID).setOnClickListener(v -> checkPermissionAndOpenCamera()); // Set the click listener for the verify ID button
        view.findViewById(R.id.btnVerifySelfie).setOnClickListener(v -> checkPermissionAndOpenCamera()); // Set the click listener for the verify selfie button
        btnSaveTicket.setOnClickListener(v -> saveTicketToFirestore()); // Set the click listener for the save ticket button

        if (getArguments() != null && getArguments().containsKey("editTicketId")) { // If we try to edit a ticket, get the ticket ID from the arguments
            editTicketId = getArguments().getString("editTicketId"); // Set the ticket ID
            loadTicketDataForEdit(editTicketId); // Load the ticket data for edit
        } else { // If we are creating a new ticket, check the verification status
            checkVerificationStatus(); // Check the verification status of the user
        }
    }

    private void initViews(View view) { // Initialize the views
        layoutStepID = view.findViewById(R.id.layoutStepID); // Identify ID
        layoutStepSelfie = view.findViewById(R.id.layoutStepSelfie); // Identify Selfie
        layoutStepDetails = view.findViewById(R.id.layoutStepDetails); // Add Details to the ticket
        ivIDCard = view.findViewById(R.id.ivIDCard);
        ivSelfie = view.findViewById(R.id.ivSelfie);
        ivTicketImage = view.findViewById(R.id.ivTicketImage);
        layoutUploadPrompt = view.findViewById(R.id.layoutUploadPrompt);
        etEventName = view.findViewById(R.id.etEventName);
        etPrice = view.findViewById(R.id.etPrice);
        etOriginalPrice = view.findViewById(R.id.etOriginalPrice);
        etEventDate = view.findViewById(R.id.etEventDate);
        etEventTime = view.findViewById(R.id.etEventTime);
        etLocation = view.findViewById(R.id.etLocation);
        etExactAddress = view.findViewById(R.id.etExactAddress);
        etQuantity = view.findViewById(R.id.etQuantity);
        spCategory = view.findViewById(R.id.spCategory);
        btnSaveTicket = view.findViewById(R.id.btnSaveTicket);
    }

    private void processIDCard(Bitmap bitmap) { // Process the ID card
        InputImage image = InputImage.fromBitmap(bitmap, 0); // Create an InputImage from the bitmap by ML kit
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build();
        FaceDetector detector = FaceDetection.getClient(options); // Create a FaceDetector

        detector.process(image).addOnSuccessListener(faces -> { // If the faces are detected, draw a box around them and set the image to the ImageView
            ivIDCard.setImageBitmap(drawFaceBox(bitmap, faces));
            checkIDNumberWithOCR(bitmap); // Check the ID number with OCR
        }).addOnFailureListener(e -> checkIDNumberWithOCR(bitmap)); // If there is an error to detect the faces, check the ID number with OCR
    }

    private void checkIDNumberWithOCR(Bitmap bitmap) { // Check the ID number with OCR
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS); // Create a TextRecognizer
        recognizer.process(InputImage.fromBitmap(bitmap, 0)).addOnSuccessListener(text -> { // Recognize the text in the image
            String cleanText = text.getText().replaceAll("[^0-9]", ""); // Recognize only numbers between 0-9
            if (cleanText.contains(userRegisteredID)) { // If the text contains the user registered ID, set the image to the ImageView
                tempIdCardBase64 = encodeToBase64(bitmap, 50); // Encode the bitmap to a base64 string in order to save it in the database
                Toast.makeText(getContext(), "תעודת זהות אומתה בהצלחה", Toast.LENGTH_SHORT).show();
                layoutStepID.setVisibility(View.GONE); // Set the visibility of the layout to the ID card to off
                layoutStepSelfie.setVisibility(View.VISIBLE); // Set the visibility of the layout to the selfie to on
            } else { // If the text does not contain the user registered ID, show a toast message
                Toast.makeText(getContext(), "מספר תעודה לא זוהה. ודא שהתמונה ברורה ומכילה את המספר: " + userRegisteredID, Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> { // If there is an error recognizing the text, show a toast message
            Toast.makeText(getContext(), "שגיאה בסריקת הטקסט", Toast.LENGTH_SHORT).show();
        });
    }

    private void processSelfie(Bitmap bitmap) { // Selfie process
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                // Recognize face
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.1f) // Recognize small face
                .build();

        FaceDetector detector = FaceDetection.getClient(options); // Create a FaceDetector

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        detector.process(image)
                .addOnSuccessListener(faces -> { // If the faces are detected
                    if (faces != null && !faces.isEmpty()) {
                        tempSelfieBase64 = encodeToBase64(bitmap, 50); // Encode the bitmap to a base64 string in order to save it in the database
                        ivSelfie.setImageBitmap(bitmap); // Set the image to the ImageView
                        Toast.makeText(getContext(), "סלפי אומת! נמצאו " + faces.size() + " פנים", Toast.LENGTH_SHORT).show(); // Show a toast message
                        uploadVerificationStatus(); // Upload the verification status
                    } else { // If the faces are not detected, show a toast message
                        Log.d("FaceDebug", "No faces found in original bitmap");
                        Toast.makeText(getContext(), "לא זוהו פנים, ודא שהפנים במרכז הפריים", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> { // If there is an error detecting the faces, show a toast message
                    Toast.makeText(getContext(), "שגיאה טכנית במנוע הזיהוי", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadVerificationStatus() { // Upload the verification status to the database
        db.collection("users").document(currentUserId) // Update the user document in the database
                .update("isVerified", true, "idCardBase64", tempIdCardBase64, "selfieImageBase64", tempSelfieBase64) // Set the verification status to true and the images to the database
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "הפרופיל אומת במערכת", Toast.LENGTH_SHORT).show(); // Show a toast message
                    showDetailsStep(); // Show the details step
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "שגיאה בעדכון הנתונים", Toast.LENGTH_SHORT).show());
    }

    private void saveTicketToFirestore() { // Save the ticket to the database
        // Save the ticket to the database
        String name = etEventName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String origPriceStr = etOriginalPrice.getText().toString().trim();
        String dateStr = etEventDate.getText().toString().trim();
        String timeStr = etEventTime.getText().toString().trim();
        String locStr = etLocation.getText().toString().trim();
        String addressStr = etExactAddress.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String categoryStr = spCategory.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || origPriceStr.isEmpty() || tempTicketBase64.isEmpty()) { // If the name, price, original price, or ticket image is empty, show a toast message
            Toast.makeText(getContext(), "נא למלא את כל השדות ולהעלות תמונה", Toast.LENGTH_SHORT).show();
            return;
        }

        double asking = Double.parseDouble(priceStr); // Convert the price to double
        double original = Double.parseDouble(origPriceStr); // Convert the original price to double


        if (asking > original) { // If the asking price is higher than the original price, show a toast message
            etPrice.setError("ספסרות אסורה! מחיר המכירה חייב להיות נמוך או שווה למחיר המקורי.");
            Toast.makeText(getContext(), "לא ניתן לפרסם במחיר גבוה מהמקור", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = 1; // Set the quantity to 1 by default
        try { if (!quantityStr.isEmpty()) quantity = Integer.parseInt(quantityStr); } catch (Exception e) {} // Try to parse the quantity from the string

        btnSaveTicket.setEnabled(false); // Disable the button
        btnSaveTicket.setText("מעבד..."); // Set the text of the button

        Map<String, Object> ticket = new HashMap<>(); // Create a new ticket
        ticket.put("sellerId", currentUserId);
        ticket.put("eventName", name);
        ticket.put("askingPrice", asking);
        ticket.put("originalPrice", original);
        ticket.put("eventDate", dateStr);
        ticket.put("eventTime", timeStr);
        ticket.put("location", locStr);
        ticket.put("exactAddress", addressStr);
        ticket.put("quantity", quantity);
        ticket.put("category", categoryStr);
        ticket.put("ticketImage", tempTicketBase64);
        ticket.put("isSold", false);
        ticket.put("timestamp", System.currentTimeMillis());

        if (editTicketId != null) { // If we are editing a ticket, update the ticket in the database
            db.collection("tickets").document(editTicketId).set(ticket, SetOptions.merge()) // Update the ticket in the database
                    .addOnSuccessListener(aVoid -> { // If the ticket is updated, show a toast message
                        Toast.makeText(getContext(), "הכרטיס עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack(); // Pop the back stack
                    })
                    .addOnFailureListener(e -> { // If there is an error updating the ticket, show a toast message
                        btnSaveTicket.setEnabled(true); // Enable the button
                        btnSaveTicket.setText("עדכן כרטיס"); // Set the text of the button
                        Toast.makeText(getContext(), "שגיאה בעדכון", Toast.LENGTH_SHORT).show(); // Show a toast message
                    });
        } else { // If we are creating a new ticket, add it to the database
            db.collection("tickets").add(ticket) // Add the ticket to the database
                    .addOnSuccessListener(doc -> { // If the ticket is added, show a toast message
                        Toast.makeText(getContext(), "הכרטיס פורסם בהצלחה!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack(); // Pop the back stack
                    })
                    .addOnFailureListener(e -> { // If there is an error adding the ticket, show a toast message
                        btnSaveTicket.setEnabled(true); // Enable the button
                        btnSaveTicket.setText("פרסם כרטיס למכירה"); // Set the text of the button
                        Toast.makeText(getContext(), "שגיאה בפרסום", Toast.LENGTH_SHORT).show(); // Show a toast message
                    });
        }
    }

    private void loadTicketDataForEdit(String ticketId) { // Load the ticket data for edit
        db.collection("tickets").document(ticketId).get().addOnSuccessListener(doc -> { // Get the ticket from the database
            if (doc.exists()) { // If the ticket exists, set the fields
                etEventName.setText(doc.getString("eventName"));
                etPrice.setText(String.valueOf(doc.getDouble("askingPrice")));
                etOriginalPrice.setText(String.valueOf(doc.getDouble("originalPrice")));
                etEventDate.setText(doc.getString("eventDate"));
                etEventTime.setText(doc.getString("eventTime"));
                etLocation.setText(doc.getString("location"), false);
                etExactAddress.setText(doc.getString("exactAddress"));
                etQuantity.setText(String.valueOf(doc.get("quantity")));
                spCategory.setText(doc.getString("category"), false);

                tempTicketBase64 = doc.getString("ticketImage");
                if (tempTicketBase64 != null && !tempTicketBase64.isEmpty()) { // If the ticket image is not empty, decode it and set it as the image for the ImageView
                    byte[] decoded = Base64.decode(tempTicketBase64, Base64.DEFAULT); // Decode the image from base64 to a byte array because the image is stored as a string in the database to save space in the database
                    ivTicketImage.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.length)); // Decode the byte array to a Bitmap and set it as the image for the ImageView
                    if (layoutUploadPrompt != null) layoutUploadPrompt.setVisibility(View.GONE); // Hide the upload prompt if picture was uploaded
                }
                btnSaveTicket.setText("עדכן כרטיס"); // Set the text of the button
                showDetailsStep(); // Show the details step
            }
        });
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // Create an intent to open the camera
        File f = new File(requireContext().getExternalCacheDir(), "temp.jpg"); // Create a temporary file to save the image
        photoUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", f); // Create a URI for the file
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri); // Add the URI to the intent
        cameraLauncher.launch(intent); // Launch the camera
    }

    private void fetchUserRegisteredData() {
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> { // Get the user from the database
            if (doc.exists()) userRegisteredID = doc.getString("idNumber"); // Set the user registered ID
        });
    }

    private void checkVerificationStatus() { // Check the verification status of the user
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> { // Get the user from the database
            if (doc.exists() && Boolean.TRUE.equals(doc.getBoolean("isVerified"))) showDetailsStep();// If the user is verified, show the details step
        });
    }

    private void showDetailsStep() {
        layoutStepID.setVisibility(View.GONE);
        layoutStepSelfie.setVisibility(View.GONE);
        layoutStepDetails.setVisibility(View.VISIBLE); // Show the details step
    }

    private void showDatePicker() { // Show the date picker
        Calendar cal = Calendar.getInstance(); // Create a calendar instance
        new DatePickerDialog(getContext(), (v, year, month, day) -> { // Create a date picker dialog
            etEventDate.setText(day + "/" + (month + 1) + "/" + year); // Set the date in the edit text field
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show(); // Show the date picker dialog
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance(); // Create a calendar instance
        new TimePickerDialog(getContext(), (v, hour, min) -> { // Create a time picker dialog
            etEventTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, min)); // Set the time in the edit text field
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show(); // Show the time picker dialog
    }

    private void setupLocationAutoComplete() { // Set up the location auto complete
        etLocation.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, ISRAEL_CITIES)); // Set the adapter for the auto complete text view
    }

    private void setupCategoryDropdown() { // Set up the category dropdown
        spCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, CATEGORIES)); // Set the adapter for the spinner
    }

    private void checkPermissionAndOpenCamera() { // Check the permission for the camera
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) { // If the permission is granted, open the camera
            openCamera();
        } else { // If the permission is not granted, request the permission
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private Bitmap scaleToSize(Bitmap b, int size) { // Scale the image to a smaller size to save space in the database and prevent crashing
        float ratio = Math.min((float)size/b.getWidth(), (float)size/b.getHeight()); // Calculate the ratio to scale the image
        return Bitmap.createScaledBitmap(b, (int)(b.getWidth()*ratio), (int)(b.getHeight()*ratio), true); // Scale the image
    }

    private Bitmap scaleAndFixImage(Bitmap b) { return scaleToSize(b, 1000); } // Scale and fix the image to a smaller size to save space in the database and prevent crashing

    private String encodeToBase64(Bitmap bitmap, int quality) { // Encode the bitmap to a base64 string in order to save it in the database
        ByteArrayOutputStream out = new ByteArrayOutputStream(); // Create a byte array output stream
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out); // Compress the bitmap to a JPEG format with the given quality
        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT); // Encode the byte array to a base64 string
    }

    private Bitmap drawFaceBox(Bitmap bitmap, List<Face> faces) { // Draw a box around the faces
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true); // Create a mutable bitmap from the original bitmap
        Canvas canvas = new Canvas(mutableBitmap); // Create a canvas from the mutable bitmap
        Paint paint = new Paint(); paint.setColor(Color.GREEN); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(5f); // Create a paint object to draw the box
        for (Face face : faces) canvas.drawRect(face.getBoundingBox(), paint); // Draw a box around the faces
        return mutableBitmap; // Return the mutable bitmap
    }
}