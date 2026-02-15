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

    private View layoutStepID, layoutStepSelfie, layoutStepDetails;
    private ImageView ivIDCard, ivSelfie, ivTicketImage;
    private LinearLayout layoutUploadPrompt;
    private EditText etEventName, etPrice, etOriginalPrice, etEventDate, etEventTime, etExactAddress, etQuantity;
    private AutoCompleteTextView etLocation, spCategory;
    private Button btnSaveTicket;
    private FirebaseFirestore db;
    private String currentUserId;
    private String tempIdCardBase64 = "", tempSelfieBase64 = "", tempTicketBase64 = "";
    private String editTicketId = null;

    private Uri photoUri;
    private String userRegisteredID = "";

    private static final String[] ISRAEL_CITIES = {"תל אביב - יפו", "ירושלים", "חיפה", "ראשון לציון", "פתח תקווה", "אשדוד", "נתניה", "באר שבע", "בני ברק", "חולון", "רמת גן", "רחובות", "אשקלון", "בת ים", "בית שמש", "כפר סבא", "הרצליה", "חדרה", "מודיעין-מכבים-רעות", "רעננה", "רמלה", "רהט", "גבעתאיים", "הוד השרון", "נהריה", "קריית אתא", "קריית גת", "אילת", "עכו", "כרמיאל", "קריית מוצקין", "רמת השרון", "עפולה", "נס ציונה", "קריית ים", "קריית ביאליק", "אור יהודה", "מעלה אדומים", "צפת", "דימונה", "טבריה", "יבנה", "נתיבות", "קריית שמונה", "נשר", "קריית מלאכי", "מעלות-תרשיחא", "שדרות"};
    private static final String[] CATEGORIES = {"הופעות", "ספורט", "קולנוע", "מסיבות", "אחר"};

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) openCamera(); else Toast.makeText(getContext(), "נדרשת הרשאת מצלמה לאימות", Toast.LENGTH_SHORT).show();
    });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == getActivity().RESULT_OK) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), photoUri);
                Bitmap optimizedBitmap = scaleAndFixImage(bitmap);
                if (layoutStepID.getVisibility() == View.VISIBLE) processIDCard(optimizedBitmap);
                else processSelfie(optimizedBitmap);
            } catch (Exception e) {
                Log.e("CameraError", "Load failed", e);
                Toast.makeText(getContext(), "שגיאה בטעינת התמונה", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private final ActivityResultLauncher<String> ticketImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                Bitmap smallBitmap = scaleToSize(bitmap, 800);
                ivTicketImage.setImageBitmap(smallBitmap);
                if (layoutUploadPrompt != null) layoutUploadPrompt.setVisibility(View.GONE);
                tempTicketBase64 = encodeToBase64(smallBitmap, 50);
            } catch (IOException e) { Log.e("AddTicket", "Error picking image", e); }
        }
    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_ticket, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initViews(view);
        setupCategoryDropdown();
        setupLocationAutoComplete();
        fetchUserRegisteredData();

        etEventDate.setOnClickListener(v -> showDatePicker());
        etEventTime.setOnClickListener(v -> showTimePicker());
        view.findViewById(R.id.frameUploadTicket).setOnClickListener(v -> ticketImageLauncher.launch("image/*"));
        view.findViewById(R.id.btnVerifyID).setOnClickListener(v -> checkPermissionAndOpenCamera());
        view.findViewById(R.id.btnVerifySelfie).setOnClickListener(v -> checkPermissionAndOpenCamera());
        btnSaveTicket.setOnClickListener(v -> saveTicketToFirestore());

        if (getArguments() != null && getArguments().containsKey("editTicketId")) {
            editTicketId = getArguments().getString("editTicketId");
            loadTicketDataForEdit(editTicketId);
        } else {
            checkVerificationStatus();
        }
    }

    private void initViews(View view) {
        layoutStepID = view.findViewById(R.id.layoutStepID);
        layoutStepSelfie = view.findViewById(R.id.layoutStepSelfie);
        layoutStepDetails = view.findViewById(R.id.layoutStepDetails);
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

    private void processIDCard(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build();
        FaceDetector detector = FaceDetection.getClient(options);

        detector.process(image).addOnSuccessListener(faces -> {
            ivIDCard.setImageBitmap(drawFaceBox(bitmap, faces));
            checkIDNumberWithOCR(bitmap);
        }).addOnFailureListener(e -> checkIDNumberWithOCR(bitmap));
    }

    private void checkIDNumberWithOCR(Bitmap bitmap) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(InputImage.fromBitmap(bitmap, 0)).addOnSuccessListener(text -> {
            String cleanText = text.getText().replaceAll("[^0-9]", "");
            if (cleanText.contains(userRegisteredID)) {
                tempIdCardBase64 = encodeToBase64(bitmap, 50);
                Toast.makeText(getContext(), "תעודת זהות אומתה בהצלחה", Toast.LENGTH_SHORT).show();
                layoutStepID.setVisibility(View.GONE);
                layoutStepSelfie.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getContext(), "מספר תעודה לא זוהה. ודא שהתמונה ברורה ומכילה את המספר: " + userRegisteredID, Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "שגיאה בסריקת הטקסט", Toast.LENGTH_SHORT).show();
        });
    }

    private void processSelfie(Bitmap bitmap) {
        // שלב 1: הגדרת מזהה עם רגישות גבוהה יותר
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE) // שינוי לדיוק גבוה
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.1f) // זיהוי פנים גם אם הם קטנים יחסית בפריים
                .build();

        FaceDetector detector = FaceDetection.getClient(options);

        // שלב 2: יצירת תמונה עם הגדרת סיבוב (Rotation 0 כברירת מחדל)
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        detector.process(image)
                .addOnSuccessListener(faces -> {
                    if (faces != null && !faces.isEmpty()) {
                        tempSelfieBase64 = encodeToBase64(bitmap, 50);
                        ivSelfie.setImageBitmap(bitmap);
                        Toast.makeText(getContext(), "סלפי אומת! נמצאו " + faces.size() + " פנים", Toast.LENGTH_SHORT).show();
                        uploadVerificationStatus();
                    } else {
                        // אם עדיין לא מזהה, ננסה "לעזור" למודל ולהקטין את התמונה עוד קצת
                        Log.d("FaceDebug", "No faces found in original bitmap");
                        Toast.makeText(getContext(), "לא זוהו פנים, ודא שהפנים במרכז הפריים", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה טכנית במנוע הזיהוי", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadVerificationStatus() {
        db.collection("users").document(currentUserId)
                .update("isVerified", true, "idCardBase64", tempIdCardBase64, "selfieImageBase64", tempSelfieBase64)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "הפרופיל אומת במערכת", Toast.LENGTH_SHORT).show();
                    showDetailsStep();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "שגיאה בעדכון הנתונים", Toast.LENGTH_SHORT).show());
    }

    private void saveTicketToFirestore() {
        String name = etEventName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String origPriceStr = etOriginalPrice.getText().toString().trim();
        String dateStr = etEventDate.getText().toString().trim();
        String timeStr = etEventTime.getText().toString().trim();
        String locStr = etLocation.getText().toString().trim();
        String addressStr = etExactAddress.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String categoryStr = spCategory.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || origPriceStr.isEmpty() || tempTicketBase64.isEmpty()) {
            Toast.makeText(getContext(), "נא למלא את כל השדות ולהעלות תמונה", Toast.LENGTH_SHORT).show();
            return;
        }

        double asking = Double.parseDouble(priceStr);
        double original = Double.parseDouble(origPriceStr);

        if (asking > original) {
            etPrice.setError("ספסרות אסורה! מחיר המכירה חייב להיות נמוך או שווה למחיר המקורי.");
            Toast.makeText(getContext(), "לא ניתן לפרסם במחיר גבוה מהמקור", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = 1;
        try { if (!quantityStr.isEmpty()) quantity = Integer.parseInt(quantityStr); } catch (Exception e) {}

        btnSaveTicket.setEnabled(false);
        btnSaveTicket.setText("מעבד...");

        Map<String, Object> ticket = new HashMap<>();
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

        if (editTicketId != null) {
            db.collection("tickets").document(editTicketId).set(ticket, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "הכרטיס עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        btnSaveTicket.setEnabled(true);
                        btnSaveTicket.setText("עדכן כרטיס");
                        Toast.makeText(getContext(), "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("tickets").add(ticket)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(getContext(), "הכרטיס פורסם בהצלחה!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        btnSaveTicket.setEnabled(true);
                        btnSaveTicket.setText("פרסם כרטיס למכירה");
                        Toast.makeText(getContext(), "שגיאה בפרסום", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadTicketDataForEdit(String ticketId) {
        db.collection("tickets").document(ticketId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
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
                if (tempTicketBase64 != null && !tempTicketBase64.isEmpty()) {
                    byte[] decoded = Base64.decode(tempTicketBase64, Base64.DEFAULT);
                    ivTicketImage.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.length));
                    if (layoutUploadPrompt != null) layoutUploadPrompt.setVisibility(View.GONE);
                }
                btnSaveTicket.setText("עדכן כרטיס");
                showDetailsStep();
            }
        });
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(requireContext().getExternalCacheDir(), "temp.jpg");
        photoUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", f);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        cameraLauncher.launch(intent);
    }

    private void fetchUserRegisteredData() {
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) userRegisteredID = doc.getString("idNumber");
        });
    }

    private void checkVerificationStatus() {
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists() && Boolean.TRUE.equals(doc.getBoolean("isVerified"))) showDetailsStep();
        });
    }

    private void showDetailsStep() {
        layoutStepID.setVisibility(View.GONE);
        layoutStepSelfie.setVisibility(View.GONE);
        layoutStepDetails.setVisibility(View.VISIBLE);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(getContext(), (v, year, month, day) -> {
            etEventDate.setText(day + "/" + (month + 1) + "/" + year);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(getContext(), (v, hour, min) -> {
            etEventTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, min));
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void setupLocationAutoComplete() {
        etLocation.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, ISRAEL_CITIES));
    }

    private void setupCategoryDropdown() {
        spCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, CATEGORIES));
    }

    private void checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private Bitmap scaleToSize(Bitmap b, int size) {
        float ratio = Math.min((float)size/b.getWidth(), (float)size/b.getHeight());
        return Bitmap.createScaledBitmap(b, (int)(b.getWidth()*ratio), (int)(b.getHeight()*ratio), true);
    }

    private Bitmap scaleAndFixImage(Bitmap b) { return scaleToSize(b, 1000); }

    private String encodeToBase64(Bitmap bitmap, int quality) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
    }

    private Bitmap drawFaceBox(Bitmap bitmap, List<Face> faces) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint(); paint.setColor(Color.GREEN); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(5f);
        for (Face face : faces) canvas.drawRect(face.getBoundingBox(), paint);
        return mutableBitmap;
    }
}