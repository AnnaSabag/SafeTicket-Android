package com.safeticket.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.safeticket.R;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTicketFragment extends Fragment {

    private View layoutStepID, layoutStepSelfie, layoutStepDetails;
    private ImageView ivIDCard, ivSelfie, ivTicketImage;
    private EditText etEventName, etPrice, etOriginalPrice, etEventDate, etEventTime;
    private AutoCompleteTextView etLocation, spCategory; // שניהם עכשיו AutoComplete למראה אחיד
    private Button btnSaveTicket;
    private FirebaseFirestore db;
    private String currentUserId;
    private String tempIdCardBase64 = "", tempSelfieBase64 = "", tempTicketBase64 = "";

    private static final String[] ISRAEL_CITIES = {
            "תל אביב - יפו", "ירושלים", "חיפה", "ראשון לציון", "פתח תקווה", "אשדוד", "נתניה", "באר שבע",
            "בני ברק", "חולון", "רמת גן", "רחובות", "אשקלון", "בת ים", "בית שמש", "כפר סבא", "הרצליה",
            "חדרה", "מודיעין-מכבים-רעות", "רעננה", "רמלה", "רהט", "גבעתאיים", "הוד השרון", "נהריה",
            "קריית אתא", "קריית גת", "אילת", "עכו", "כרמיאל", "קריית מוצקין", "רמת השרון", "עפולה",
            "נס ציונה", "קריית ים", "קריית ביאליק", "אור יהודה", "מעלה אדומים", "צפת", "דימונה", "טבריה",
            "יבנה", "נתיבות", "קריית שמונה", "נשר", "קריית מלאכי", "מעלות-תרשיחא", "שדרות"
    };

    private static final String[] CATEGORIES = {"הופעות", "ספורט", "קולנוע", "מסיבות", "אחר"};

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openCamera();
                else Toast.makeText(getContext(), "נדרשת הרשאת מצלמה", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    if (layoutStepID.getVisibility() == View.VISIBLE) {
                        processImageWithFaceDetection(bitmap, ivIDCard, true);
                    } else {
                        processImageWithFaceDetection(bitmap, ivSelfie, false);
                    }
                }
            });

    private final ActivityResultLauncher<String> ticketImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                        ivTicketImage.setImageBitmap(bitmap);
                        tempTicketBase64 = encodeToBase64(bitmap, 30);
                    } catch (IOException e) {
                        Log.e("AddTicket", "Error picking image", e);
                    }
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
        setupCategoryDropdown(); // הפונקציה המעודכנת
        setupLocationAutoComplete();
        checkVerificationStatus();

        etEventDate.setOnClickListener(v -> showDatePicker());
        etEventTime.setOnClickListener(v -> showTimePicker());
        view.findViewById(R.id.btnVerifyID).setOnClickListener(v -> checkPermissionAndOpenCamera());
        view.findViewById(R.id.btnVerifySelfie).setOnClickListener(v -> checkPermissionAndOpenCamera());
        ivTicketImage.setOnClickListener(v -> ticketImageLauncher.launch("image/*"));
        btnSaveTicket.setOnClickListener(v -> saveTicketToFirestore());
    }

    private void initViews(View view) {
        layoutStepID = view.findViewById(R.id.layoutStepID);
        layoutStepSelfie = view.findViewById(R.id.layoutStepSelfie);
        layoutStepDetails = view.findViewById(R.id.layoutStepDetails);
        ivIDCard = view.findViewById(R.id.ivIDCard);
        ivSelfie = view.findViewById(R.id.ivSelfie);
        ivTicketImage = view.findViewById(R.id.ivTicketImage);
        etEventName = view.findViewById(R.id.etEventName);
        etPrice = view.findViewById(R.id.etPrice);
        etOriginalPrice = view.findViewById(R.id.etOriginalPrice);
        etLocation = view.findViewById(R.id.etLocation);
        etEventDate = view.findViewById(R.id.etEventDate);
        etEventTime = view.findViewById(R.id.etEventTime);
        spCategory = view.findViewById(R.id.spCategory); // עכשיו AutoCompleteTextView
        btnSaveTicket = view.findViewById(R.id.btnSaveTicket);
    }

    private void setupLocationAutoComplete() {
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, ISRAEL_CITIES);
        etLocation.setAdapter(cityAdapter);
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        spCategory.setAdapter(categoryAdapter);
        // הגדרת ערך ברירת מחדל
        spCategory.setText(CATEGORIES[0], false);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            etEventDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            etEventTime.setText(String.format("%02d:%02d", hourOfDay, minute));
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(takePictureIntent);
    }

    private void processImageWithFaceDetection(Bitmap bitmap, ImageView imageView, boolean isIDCard) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        FaceDetector detector = FaceDetection.getClient(new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setMinFaceSize(0.1f).build());

        detector.process(image).addOnSuccessListener(faces -> {
            if (!faces.isEmpty()) {
                Bitmap drawnBitmap = drawFaceBox(bitmap, faces);
                imageView.setImageBitmap(drawnBitmap);
                if (isIDCard) {
                    tempIdCardBase64 = encodeToBase64(bitmap, 40);
                    layoutStepID.setVisibility(View.GONE);
                    layoutStepSelfie.setVisibility(View.VISIBLE);
                } else {
                    tempSelfieBase64 = encodeToBase64(bitmap, 40);
                    uploadVerificationStatus();
                }
            } else {
                Toast.makeText(getContext(), "לא זוהו פנים, נסה שוב", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveTicketToFirestore() {
        String name = etEventName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String date = etEventDate.getText().toString().trim();
        String locationStr = etLocation.getText().toString().trim();
        String categoryStr = spCategory.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || date.isEmpty() || tempTicketBase64.isEmpty() || locationStr.isEmpty()) {
            Toast.makeText(getContext(), "נא למלא פרטי חובה, מיקום ותמונה", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveTicket.setEnabled(false);
        btnSaveTicket.setText("מעבד...");

        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("sellerId", currentUserId);
        ticketData.put("eventName", name);
        ticketData.put("askingPrice", Double.parseDouble(priceStr));
        ticketData.put("originalPrice", etOriginalPrice.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(etOriginalPrice.getText().toString()));
        ticketData.put("location", locationStr);
        ticketData.put("category", categoryStr);
        ticketData.put("eventDate", date);
        ticketData.put("eventTime", etEventTime.getText().toString());
        ticketData.put("ticketImage", tempTicketBase64);
        ticketData.put("isActive", true);
        ticketData.put("timestamp", System.currentTimeMillis());

        String editId = (getArguments() != null) ? getArguments().getString("editTicketId") : null;

        if (editId != null) {
            db.collection("tickets").document(editId).set(ticketData)
                    .addOnSuccessListener(aVoid -> handleSuccess("הכרטיס עודכן!"))
                    .addOnFailureListener(e -> handleFailure());
        } else {
            db.collection("tickets").add(ticketData)
                    .addOnSuccessListener(doc -> handleSuccess("הכרטיס פורסם!"))
                    .addOnFailureListener(e -> handleFailure());
        }
    }

    private void handleSuccess(String msg) {
        if (isAdded()) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        }
    }

    private void handleFailure() {
        btnSaveTicket.setEnabled(true);
        btnSaveTicket.setText("נסה שוב");
        Toast.makeText(getContext(), "שגיאה בתקשורת עם השרת", Toast.LENGTH_SHORT).show();
    }

    private void uploadVerificationStatus() {
        db.collection("users").document(currentUserId)
                .update("isVerified", true, "idCardBase64", tempIdCardBase64, "selfieImageBase64", tempSelfieBase64)
                .addOnSuccessListener(aVoid -> showDetailsStep());
    }

    private Bitmap drawFaceBox(Bitmap bitmap, List<Face> faces) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        for (Face face : faces) canvas.drawRect(face.getBoundingBox(), paint);
        return mutableBitmap;
    }

    private void checkVerificationStatus() {
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists() && Boolean.TRUE.equals(doc.getBoolean("isVerified"))) showDetailsStep();
            else layoutStepID.setVisibility(View.VISIBLE);
        });
    }

    private void showDetailsStep() {
        layoutStepID.setVisibility(View.GONE);
        layoutStepSelfie.setVisibility(View.GONE);
        layoutStepDetails.setVisibility(View.VISIBLE);
    }

    private String encodeToBase64(Bitmap bitmap, int quality) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
    }
}