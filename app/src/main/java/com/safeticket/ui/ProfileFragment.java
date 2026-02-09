package com.safeticket.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.safeticket.R;
import com.safeticket.adapter.TicketAdapter;
import com.safeticket.model.Ticket;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private ImageView ivProfileImage, ivVerificationIcon;
    private TextView tvProfileName, tvProfileEmail, tvVerificationStatus, tvEmptyMessage;
    private RecyclerView rvMyTickets;
    private TicketAdapter adapter;
    private List<Ticket> myTicketsList;
    private FirebaseFirestore db;
    private String currentUserId;
    private LinearLayout layoutVerification;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    saveProfileImage(result.getData().getData());
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        initViews(view);
        setupRecyclerView();
        fetchUserData();
        fetchMyTickets();

        ivProfileImage.setOnClickListener(v -> galleryLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)));
        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });
        // בתוך onViewCreated ב-ProfileFragment.java
        view.findViewById(R.id.ivProfileImage).requestFocus();
    }

    private void initViews(View view) {
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        ivVerificationIcon = view.findViewById(R.id.ivVerificationIcon);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvVerificationStatus = view.findViewById(R.id.tvVerificationStatus);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        rvMyTickets = view.findViewById(R.id.rvMyTickets);
        layoutVerification = view.findViewById(R.id.layoutVerification);
    }

    private void setupRecyclerView() {
        myTicketsList = new ArrayList<>();
        rvMyTickets.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TicketAdapter(myTicketsList, true, new TicketAdapter.OnTicketClickListener() {
            @Override
            public void onTicketClick(Ticket ticket) {
                // מעבר למסך פרטי כרטיס
                TicketDetailsFragment detailsFragment = new TicketDetailsFragment();
                Bundle args = new Bundle();
                args.putString("eventName", ticket.getEventName());
                args.putString("price", ticket.getAskingPrice() + " ₪");
                args.putString("location", ticket.getLocation());
                args.putString("sellerId", ticket.getSellerId());
                args.putString("ticketImage", ticket.getTicketImage());
                detailsFragment.setArguments(args);
                getParentFragmentManager().beginTransaction().replace(R.id.main_container, detailsFragment).addToBackStack(null).commit();
            }

            @Override
            public void onEditClick(Ticket ticket) {
                AddTicketFragment editFragment = new AddTicketFragment();
                Bundle args = new Bundle();
                args.putString("editTicketId", ticket.getTicketId());
                editFragment.setArguments(args);
                getParentFragmentManager().beginTransaction().replace(R.id.main_container, editFragment).addToBackStack(null).commit();
            }

            @Override
            public void onDeleteClick(Ticket ticket) {
                confirmDelete(ticket);
            }
        });
        rvMyTickets.setAdapter(adapter);
    }

    private void confirmDelete(Ticket ticket) {
        new AlertDialog.Builder(getContext())
                .setTitle("מחיקת כרטיס")
                .setMessage("האם אתה בטוח שברצונך למחוק את הכרטיס ל-" + ticket.getEventName() + "?")
                .setPositiveButton("מחק", (dialog, which) -> {
                    db.collection("tickets").document(ticket.getTicketId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "הכרטיס נמחק בהצלחה", Toast.LENGTH_SHORT).show();
                                // התיקון: רענון מיידי של הרשימה מה-DB
                                fetchMyTickets();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "שגיאה במחיקה", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void fetchUserData() {
        if (currentUserId == null) return;
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String firstName = doc.getString("firstName");
                String lastName = doc.getString("lastName");
                tvProfileName.setText((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""));
                tvProfileEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

                Boolean isVerified = doc.getBoolean("isVerified");
                if (isVerified != null && isVerified) {
                    tvVerificationStatus.setText("חשבון מאומת");
                    layoutVerification.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"));
                } else {
                    tvVerificationStatus.setText("חשבון לא מאומת");
                    layoutVerification.setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"));
                }

                String base64 = doc.getString("profileImageBase64");
                if (base64 != null && !base64.isEmpty()) {
                    byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                    ivProfileImage.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.length));
                }
            }
        });
    }

    private void fetchMyTickets() {
        if (currentUserId == null) return;
        db.collection("tickets")
                .whereEqualTo("sellerId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    myTicketsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Ticket ticket = doc.toObject(Ticket.class);
                        myTicketsList.add(ticket);
                    }
                    adapter.notifyDataSetChanged();

                    if (myTicketsList.isEmpty()) {
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                        rvMyTickets.setVisibility(View.GONE);
                    } else {
                        tvEmptyMessage.setVisibility(View.GONE);
                        rvMyTickets.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> Log.e("ProfileFragment", "Error fetching tickets", e));
    }

    private void saveProfileImage(Uri uri) {
        try {
            InputStream is = getContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 400, 400, true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, out);
            String base64 = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
            db.collection("users").document(currentUserId).update("profileImageBase64", base64)
                    .addOnSuccessListener(aVoid -> ivProfileImage.setImageBitmap(scaled));
        } catch (Exception e) { e.printStackTrace(); }
    }
}