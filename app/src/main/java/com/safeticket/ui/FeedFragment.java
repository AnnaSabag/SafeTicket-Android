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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedFragment extends Fragment {

    private RecyclerView rvTicketList;
    private TicketAdapter adapter;
    private List<Ticket> ticketList;
    private FirebaseFirestore db;
    private SearchView searchView;
    private String currentCategory = "הכל";

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
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
                Date today = cal.getTime();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        Ticket ticket = document.toObject(Ticket.class);
                        if (ticket != null) {
                            ticket.setTicketId(document.getId());

                            Date eventDate = sdf.parse(ticket.getEventDate());
                            // סינון: רק אם התאריך רלוונטי וגם הכרטיס לא נמכר
                            if (eventDate != null && !eventDate.before(today) && !ticket.isSold()) {
                                ticketList.add(ticket);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("FirestoreError", "Parsing error", e);
                    }
                }
                sortTicketsByDate();
                if (adapter != null) adapter.setFilteredList(new ArrayList<>(ticketList));
            }
        });
    }

    private void sortTicketsByDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Collections.sort(ticketList, (t1, t2) -> {
            try {
                Date d1 = sdf.parse(t1.getEventDate());
                Date d2 = sdf.parse(t2.getEventDate());
                return d1.compareTo(d2);
            } catch (Exception e) { return 0; }
        });
    }

    private void setupAdapter() {
        adapter = new TicketAdapter(ticketList, false, new TicketAdapter.OnTicketClickListener() {
            @Override
            public void onTicketClick(Ticket ticket) {
                TicketDetailsFragment detailsFragment = new TicketDetailsFragment();
                Bundle args = new Bundle();

                // התיקון כאן: חובה להעביר את ה-ID כדי שספירת הצפיות תעבוד!
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

                detailsFragment.setArguments(args);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.main_container, detailsFragment)
                        .addToBackStack(null).commit();
            }
            @Override public void onEditClick(Ticket ticket) {}
            @Override public void onDeleteClick(Ticket ticket) {}
            @Override public void onSoldClick(Ticket ticket) {}
        });
        rvTicketList.setAdapter(adapter);
    }

    private void filterList(String text) {
        if (adapter == null) return;
        List<Ticket> filteredList = new ArrayList<>();
        for (Ticket ticket : ticketList) {
            String name = (ticket.getEventName() != null) ? ticket.getEventName().toLowerCase() : "";
            String loc = (ticket.getLocation() != null) ? ticket.getLocation().toLowerCase() : "";
            String cat = (ticket.getCategory() != null) ? ticket.getCategory() : "";
            boolean matchesSearch = name.contains(text.toLowerCase()) || loc.contains(text.toLowerCase());
            boolean matchesCategory = currentCategory.equals("הכל") || cat.equalsIgnoreCase(currentCategory);
            if (matchesSearch && matchesCategory) filteredList.add(ticket);
        }
        adapter.setFilteredList(filteredList);
    }

    private void setupCategoryButtons(View view) {
        View.OnClickListener listener = v -> {
            currentCategory = ((Button) v).getText().toString();
            filterList(searchView.getQuery().toString());
        };
        int[] ids = {R.id.btnCatAll, R.id.btnCatConcert, R.id.btnCatSport, R.id.btnCatCinema, R.id.btnCatParties, R.id.btnCatOther};
        for (int id : ids) {
            View btn = view.findViewById(id);
            if (btn != null) btn.setOnClickListener(listener);
        }
    }
}