package com.safeticket.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.safeticket.R;
import com.safeticket.adapter.TicketAdapter;
import com.safeticket.model.Ticket;
import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private RecyclerView rvTicketList;
    private TicketAdapter adapter;
    private List<Ticket> ticketList;
    private FirebaseFirestore db;
    private SearchView searchView;
    private String currentCategory = "All";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        ticketList = new ArrayList<>();

        rvTicketList = view.findViewById(R.id.rvTicketList);
        searchView = view.findViewById(R.id.searchView);
        rvTicketList.setLayoutManager(new LinearLayoutManager(getContext()));

        setupAdapter();
        fetchTicketsFromFirestore();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        setupCategoryButtons(view);

        view.findViewById(R.id.fabAddTicket).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new AddTicketFragment())
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.ivProfileButton).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void fetchTicketsFromFirestore() {
        db.collection("tickets").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ticketList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        Ticket ticket = document.toObject(Ticket.class);
                        ticket.setTicketId(document.getId());
                        ticketList.add(ticket);
                    } catch (Exception e) {
                        Log.e("FirestoreError", "Error parsing ticket", e);
                    }
                }
                if (adapter != null) {
                    adapter.setFilteredList(new ArrayList<>(ticketList));
                }
            } else {
                Log.e("FirestoreError", "Error fetching tickets", task.getException());
            }
        });
    }

    private void setupAdapter() {
        // עדכון הבנאי כך שיעביר false (אנחנו לא במצב פרופיל, לא רוצים כפתורי עריכה/מחיקה בפיד)
        adapter = new TicketAdapter(ticketList, false, new TicketAdapter.OnTicketClickListener() {
            @Override
            public void onTicketClick(Ticket ticket) {
                TicketDetailsFragment detailsFragment = new TicketDetailsFragment();
                Bundle args = new Bundle();

                // וידוא שימוש ב-Getters המדויקים מהמודל שלך
                args.putString("eventName", ticket.getEventName());
                args.putDouble("askingPrice", ticket.getAskingPrice()); // המודל מחזיר double
                args.putString("location", ticket.getLocation());
                args.putString("sellerId", ticket.getSellerId());
                args.putString("ticketImage", ticket.getTicketImage());
                args.putString("eventDate", ticket.getEventDate()); // הוספת תאריך
                args.putString("eventTime", ticket.getEventTime()); // הוספת שעה

                detailsFragment.setArguments(args);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.main_container, detailsFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onEditClick(Ticket ticket) {
                // לא נדרש בפיד הכללי
            }

            @Override
            public void onDeleteClick(Ticket ticket) {
                // לא נדרש בפיד הכללי
            }
        });
        rvTicketList.setAdapter(adapter);
    }

    private void filterList(String text) {
        if (adapter == null) return;
        List<Ticket> filteredList = new ArrayList<>();
        for (Ticket ticket : ticketList) {
            // בדיקת בטיחות שהשדות לא null
            String name = ticket.getEventName() != null ? ticket.getEventName().toLowerCase() : "";
            String loc = ticket.getLocation() != null ? ticket.getLocation().toLowerCase() : "";
            String cat = ticket.getCategory() != null ? ticket.getCategory() : "";

            boolean matchesSearch = name.contains(text.toLowerCase()) || loc.contains(text.toLowerCase());
            boolean matchesCategory = currentCategory.equals("All") || cat.equalsIgnoreCase(currentCategory);

            if (matchesSearch && matchesCategory) filteredList.add(ticket);
        }
        adapter.setFilteredList(filteredList);
    }

    private void setupCategoryButtons(View view) {
        View.OnClickListener listener = v -> {
            currentCategory = ((Button) v).getText().toString();
            filterList(searchView.getQuery().toString());
        };
        // וודא שה-IDs האלו קיימים ב-fragment_feed.xml שלך
        if (view.findViewById(R.id.btnCatAll) != null) view.findViewById(R.id.btnCatAll).setOnClickListener(listener);
        if (view.findViewById(R.id.btnCatConcert) != null) view.findViewById(R.id.btnCatConcert).setOnClickListener(listener);
        if (view.findViewById(R.id.btnCatSport) != null) view.findViewById(R.id.btnCatSport).setOnClickListener(listener);
        if (view.findViewById(R.id.btnCatOther) != null) view.findViewById(R.id.btnCatOther).setOnClickListener(listener);
    }
}