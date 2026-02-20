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
        return inflater.inflate(R.layout.fragment_profile, container, false); // Return the inflated view
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance(); // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getUid(); // Get the current user's ID

        initViews(view); // Initialize the views
        setupRecyclerView(); // Set up the RecyclerView
        fetchUserData(); // Fetch the user's data
        fetchMyTickets(); // Fetch the user's tickets
        fetchSoldStatistics(); // Fetch the user's sold statistics

        ivProfileImage.setOnClickListener(v -> galleryLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))); // Set up the profile image click listener

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> { // Set up the edit profile button click listener
            getParentFragmentManager().beginTransaction() // Navigate to the edit profile fragment
                    .replace(R.id.main_container, new EditProfileFragment()) // Replace the current fragment with the edit profile fragment
                    .addToBackStack(null).commit(); // Add the fragment to the back stack
        });
    }

    private void initViews(View view) { // Initialize the views
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvVerificationStatus = view.findViewById(R.id.tvVerificationStatus);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        tvSoldStats = view.findViewById(R.id.tvSoldStats);
        rvMyTickets = view.findViewById(R.id.rvMyTickets);
        layoutVerification = view.findViewById(R.id.layoutVerification);
    }

    private void fetchSoldStatistics() { // Fetch the user's sold statistics
        if (currentUserId == null) return; // Check if the current user ID is null
        db.collection("tickets")
                .whereEqualTo("sellerId", currentUserId)
                .whereEqualTo("isSold", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> { // Update the sold statistics text view
                    int soldCount = queryDocumentSnapshots.size(); // Get the number of tickets sold by the user
                    if (tvSoldStats != null) { // Check if the sold statistics text view exists
                        tvSoldStats.setText("מכרת בהצלחה " + soldCount + " כרטיסים"); // Set the text
                        tvSoldStats.setVisibility(View.VISIBLE); // Show the sold statistics text view
                    }
                });
    }

    private void setupRecyclerView() { // Set up the RecyclerView
        myTicketsList = new ArrayList<>(); // Initialize the list
        rvMyTickets.setLayoutManager(new LinearLayoutManager(getContext())); // Set the layout manager
        adapter = new TicketAdapter(myTicketsList, true, new TicketAdapter.OnTicketClickListener() { // Set the adapter
            @Override
            public void onTicketClick(Ticket ticket) { // Handle the ticket click
                TicketDetailsFragment detailsFragment = new TicketDetailsFragment(); // Create the ticket details fragment
                Bundle args = new Bundle(); // Create the bundle
                // Add the ticket details to the bundle
                args.putString("ticketId", ticket.getTicketId());
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

                detailsFragment.setArguments(args); // Set the arguments

                getParentFragmentManager().beginTransaction() // Navigate to the ticket details fragment
                        .replace(R.id.main_container, detailsFragment) // Replace the current fragment with the ticket details fragment
                        .addToBackStack(null).commit(); // Add the fragment to the back stack
            }
            @Override
            public void onEditClick(Ticket ticket) { // Handle the edit click
                AddTicketFragment editFragment = new AddTicketFragment(); // Create the edit fragment
                Bundle b = new Bundle(); // Create the bundle
                b.putString("editTicketId", ticket.getTicketId()); // Add the ticket ID to the bundle
                editFragment.setArguments(b);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.main_container, editFragment)
                        .addToBackStack(null).commit();
            }
            @Override
            public void onDeleteClick(Ticket ticket) { confirmDelete(ticket); } // Handle the delete click


            @Override
            public void onSoldClick(Ticket ticket) { // Handle the sold click
                new AlertDialog.Builder(getContext())
                        .setTitle("מזל טוב!") // Set the title
                        .setMessage("לסמן את הכרטיס כנמכר? הוא יוסר מהפיד וייחשב בסטטיסטיקה.") // Set the message
                        .setPositiveButton("כן", (dialog, which) -> { // Handle the positive button click
                            db.collection("tickets").document(ticket.getTicketId())
                                    .update("isSold", true)
                                    .addOnSuccessListener(aVoid -> { // Update the ticket

                                        fetchMyTickets(); // Update the list
                                        fetchSoldStatistics(); // Update the sold statistics
                                    });
                        })
                        .setNegativeButton("ביטול", null).show(); // Show the dialog
            }
        });
        rvMyTickets.setAdapter(adapter); // Set the adapter
    }

    private void fetchMyTickets() { // Fetch the user's tickets
        if (currentUserId == null) return; // Check if the current user ID is null
        db.collection("tickets").whereEqualTo("sellerId", currentUserId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> { // Update the list
                    myTicketsList.clear();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
                    Date today = cal.getTime();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) { // Loop through the documents
                        try {
                            Ticket ticket = doc.toObject(Ticket.class); // Convert the document to a ticket object
                            if (ticket != null) {
                                ticket.setTicketId(doc.getId()); // Set the ticket ID

                                // Check if the ticket is expired and not sold
                                Date eventDate = sdf.parse(ticket.getEventDate());

                                if (eventDate != null && eventDate.before(today) && !ticket.isSold()) {
                                    db.collection("tickets").document(doc.getId()).delete(); // Delete the expired ticket
                                    continue;
                                }

                                if (!ticket.isSold()) { // Check if the ticket is not sold
                                    myTicketsList.add(ticket); // Add the ticket to the list

                                }
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                    sortTicketsByDate();
                    adapter.notifyDataSetChanged(); // Notify the adapter
                    tvEmptyMessage.setVisibility(myTicketsList.isEmpty() ? View.VISIBLE : View.GONE); // Show the empty message or hide it
                    rvMyTickets.setVisibility(myTicketsList.isEmpty() ? View.GONE : View.VISIBLE); // Show the RecyclerView or hide it
                });
    }

    private void sortTicketsByDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // Create a date format
        Collections.sort(myTicketsList, (t1, t2) -> { // Sort the list by date
            try {
                Date d1 = sdf.parse(t1.getEventDate());
                Date d2 = sdf.parse(t2.getEventDate());
                return d1.compareTo(d2); // Compare the dates, return the difference
            } catch (Exception e) { return 0; }
        });
    }

    private void confirmDelete(Ticket ticket) { // Confirm the delete
        new AlertDialog.Builder(getContext()).setTitle("מחיקה").setMessage("למחוק את הכרטיס?") // Set the title and message
                .setPositiveButton("כן", (d, w) -> { // Handle the positive button click
                    db.collection("tickets").document(ticket.getTicketId()).delete() // Delete the ticket
                            .addOnSuccessListener(aVoid -> fetchMyTickets());
                }).setNegativeButton("לא", null).show();

    }

    private void fetchUserData() { // Fetch the user's data
        if (currentUserId == null) return;
        // Check if the current user ID is null and fetch the user's data
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) { // Check if the document exists
                String f = doc.getString("firstName");
                String l = doc.getString("lastName");
                tvProfileName.setText((f != null ? f : "") + " " + (l != null ? l : "")); // Set the user's name
                tvProfileEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail()); // Set the user's email

                Boolean verified = doc.getBoolean("isVerified"); // Set the verification status
                tvVerificationStatus.setText(Boolean.TRUE.equals(verified) ? "חשבון מאומת" : "חשבון לא מאומת"); // Set the verification status
                layoutVerification.setBackgroundColor(Boolean.TRUE.equals(verified) ? 0xFFE8F5E9 : 0xFFFFEBEE); // Set the background color

                String img = doc.getString("profileImageBase64"); // Set the user's profile image
                if (img != null && !img.isEmpty()) {
                    byte[] bytes = Base64.decode(img, Base64.DEFAULT);
                    ivProfileImage.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                }
            }
        });
    }

    private void saveProfileImage(Uri uri) { // Save the user's profile image
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