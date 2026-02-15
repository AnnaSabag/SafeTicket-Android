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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private ImageView ivProfileImage;
    private TextView tvProfileName, tvProfileEmail, tvVerificationStatus, tvEmptyMessage, tvSoldStats;
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
        fetchSoldStatistics(); // משיכת סטטיסטיקה

        ivProfileImage.setOnClickListener(v -> galleryLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)));
        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new EditProfileFragment())
                    .addToBackStack(null).commit();
        });
    }

    private void initViews(View view) {
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvVerificationStatus = view.findViewById(R.id.tvVerificationStatus);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        tvSoldStats = view.findViewById(R.id.tvSoldStats); // הנחתי שיש TextView כזה ב-layout
        rvMyTickets = view.findViewById(R.id.rvMyTickets);
        layoutVerification = view.findViewById(R.id.layoutVerification);
    }

    private void fetchSoldStatistics() {
        if (currentUserId == null) return;
        db.collection("tickets")
                .whereEqualTo("sellerId", currentUserId)
                .whereEqualTo("isSold", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int soldCount = queryDocumentSnapshots.size();
                    if (tvSoldStats != null) {
                        tvSoldStats.setText("מכרת בהצלחה " + soldCount + " כרטיסים");
                        tvSoldStats.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void setupRecyclerView() {
        myTicketsList = new ArrayList<>();
        rvMyTickets.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TicketAdapter(myTicketsList, true, new TicketAdapter.OnTicketClickListener() {
            @Override
            public void onTicketClick(Ticket ticket) {
                TicketDetailsFragment detailsFragment = new TicketDetailsFragment();
                Bundle args = new Bundle();
                args.putString("ticketId", ticket.getTicketId()); // חשוב לצפיות
                args.putString("eventName", ticket.getEventName());
                args.putDouble("askingPrice", ticket.getAskingPrice());
                args.putDouble("originalPrice", ticket.getOriginalPrice());
                args.putInt("quantity", ticket.getQuantity());
                args.putString("location", ticket.getLocation());
                args.putString("exactAddress", ticket.getExactAddress());
                args.putString("sellerId", ticket.getSellerId());
                args.putString("ticketImage", ticket.getTicketImage());
                args.putString("eventDate", ticket.getEventDate());
                args.putString("eventTime", ticket.getEventTime());
                args.putString("category", ticket.getCategory());
                args.putBoolean("isExpired", false);

                detailsFragment.setArguments(args);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.main_container, detailsFragment)
                        .addToBackStack(null).commit();
            }
            @Override
            public void onEditClick(Ticket ticket) {
                AddTicketFragment editFragment = new AddTicketFragment();
                Bundle b = new Bundle();
                b.putString("editTicketId", ticket.getTicketId());
                editFragment.setArguments(b);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.main_container, editFragment)
                        .addToBackStack(null).commit();
            }
            @Override
            public void onDeleteClick(Ticket ticket) { confirmDelete(ticket); }

            @Override
            public void onSoldClick(Ticket ticket) {
                new AlertDialog.Builder(getContext())
                        .setTitle("מזל טוב!")
                        .setMessage("לסמן את הכרטיס כנמכר? הוא יוסר מהפיד וייחשב בסטטיסטיקה.")
                        .setPositiveButton("כן", (dialog, which) -> {
                            db.collection("tickets").document(ticket.getTicketId())
                                    .update("isSold", true)
                                    .addOnSuccessListener(aVoid -> {
                                        fetchMyTickets();
                                        fetchSoldStatistics(); // רענון סטטיסטיקה
                                    });
                        })
                        .setNegativeButton("ביטול", null).show();
            }
        });
        rvMyTickets.setAdapter(adapter);
    }

    private void fetchMyTickets() {
        if (currentUserId == null) return;
        db.collection("tickets").whereEqualTo("sellerId", currentUserId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    myTicketsList.clear();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
                    Date today = cal.getTime();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Ticket ticket = doc.toObject(Ticket.class);
                            if (ticket != null) {
                                ticket.setTicketId(doc.getId());
                                Date eventDate = sdf.parse(ticket.getEventDate());

                                if (eventDate != null && eventDate.before(today) && !ticket.isSold()) {
                                    db.collection("tickets").document(doc.getId()).delete();
                                    continue;
                                }

                                if (!ticket.isSold()) {
                                    myTicketsList.add(ticket);
                                }
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                    sortTicketsByDate();
                    adapter.notifyDataSetChanged();
                    tvEmptyMessage.setVisibility(myTicketsList.isEmpty() ? View.VISIBLE : View.GONE);
                    rvMyTickets.setVisibility(myTicketsList.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }

    private void sortTicketsByDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Collections.sort(myTicketsList, (t1, t2) -> {
            try {
                Date d1 = sdf.parse(t1.getEventDate());
                Date d2 = sdf.parse(t2.getEventDate());
                return d1.compareTo(d2);
            } catch (Exception e) { return 0; }
        });
    }

    private void confirmDelete(Ticket ticket) {
        new AlertDialog.Builder(getContext()).setTitle("מחיקה").setMessage("למחוק את הכרטיס?")
                .setPositiveButton("כן", (d, w) -> {
                    db.collection("tickets").document(ticket.getTicketId()).delete()
                            .addOnSuccessListener(aVoid -> fetchMyTickets());
                }).setNegativeButton("לא", null).show();
    }

    private void fetchUserData() {
        if (currentUserId == null) return;
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String f = doc.getString("firstName");
                String l = doc.getString("lastName");
                tvProfileName.setText((f != null ? f : "") + " " + (l != null ? l : ""));
                tvProfileEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

                Boolean verified = doc.getBoolean("isVerified");
                tvVerificationStatus.setText(Boolean.TRUE.equals(verified) ? "חשבון מאומת" : "חשבון לא מאומת");
                layoutVerification.setBackgroundColor(Boolean.TRUE.equals(verified) ? 0xFFE8F5E9 : 0xFFFFEBEE);

                String img = doc.getString("profileImageBase64");
                if (img != null && !img.isEmpty()) {
                    byte[] bytes = Base64.decode(img, Base64.DEFAULT);
                    ivProfileImage.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                }
            }
        });
    }

    private void saveProfileImage(Uri uri) {
        try {
            InputStream is = getContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 400, 400, true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, out);
            String b64 = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
            db.collection("users").document(currentUserId).update("profileImageBase64", b64)
                    .addOnSuccessListener(aVoid -> ivProfileImage.setImageBitmap(scaled));
        } catch (Exception e) { e.printStackTrace(); }
    }
}