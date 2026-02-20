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

    private RecyclerView rvTicketList; // Declare the RecyclerView, UI component for displaying a list of items
    private TicketAdapter adapter; // Declare the adapter, responsible for binding data to the RecyclerView
    private List<Ticket> ticketList; // Declare the list of tickets, which will be displayed in the RecyclerView
    private FirebaseFirestore db; // Declare the Firebase Firestore instance, used for accessing the database
    private SearchView searchView; //  Declare the SearchView, used for searching tickets my name or location
    private String currentCategory = "הכל"; // Declare the current category as "all", used for filtering tickets



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false); // Return the inflated view
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { // Called when the view is created
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase and the list of tickets
        db = FirebaseFirestore.getInstance();
        ticketList = new ArrayList<>();

        rvTicketList = view.findViewById(R.id.rvTicketList); // Find the RecyclerView in the layout
        searchView = view.findViewById(R.id.searchView); // Find the SearchView in the layout

        rvTicketList.setLayoutManager(new LinearLayoutManager(getContext())); // Set the layout manager for the RecyclerView, how the items will be displayed (a linear layout)

        setupAdapter(); // Set up the adapter for the RecyclerView
        fetchTicketsFromFirestore(); // Fetch tickets from Firestore

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() { // Set up the listener for the SearchView
            @Override
            public boolean onQueryTextSubmit(String query) { return false; } // No need to click on the search button to submit the query
            @Override
            public boolean onQueryTextChange(String newText) { // Called when the user types in the SearchView, filters the list of tickets based on the text
                filterList(newText); // Filter the list of tickets based on the text
                return true; // Return true to indicate that the query has been handled
            }
        });

        setupCategoryButtons(view); // Handle with the category buttons

        view.findViewById(R.id.fabAddTicket).setOnClickListener(v -> { // Handle with the add ticket button
            getParentFragmentManager().beginTransaction() // Start a fragment transaction
                    .replace(R.id.main_container, new AddTicketFragment()) // Replace the container with the AddTicketFragment
                    .addToBackStack(null) // Add the transaction to the back stack, so the user can navigate back
                    .commit(); // Do the transaction
        });

        view.findViewById(R.id.ivProfileButton).setOnClickListener(v -> { // Handle with the profile button
            getParentFragmentManager().beginTransaction() // Start a fragment transaction
                    .replace(R.id.main_container, new ProfileFragment()) // Replace the container with the ProfileFragment
                    .addToBackStack(null)// Add the transaction to the back stack, so the user can navigate back
                    .commit();// Do the transaction
        });
    }

    private void fetchTicketsFromFirestore() { // Fetch tickets from Firestore
        db.collection("tickets").get().addOnCompleteListener(task -> { // Get all documents from the "tickets" collection
            if (task.isSuccessful() && task.getResult() != null) {
                ticketList.clear(); // Clear the list of tickets to avoid duplicates
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // Set up the date format

                Calendar cal = Calendar.getInstance(); // Get the current date
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); // Clear the time
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0); // Clear the time
                Date today = cal.getTime(); // Get the current date

                for (QueryDocumentSnapshot document : task.getResult()) { // Iterate over the documents
                    try { // Try to parse the document to a Ticket object
                        Ticket ticket = document.toObject(Ticket.class);
                        if (ticket != null) { // If the ticket is not null
                            ticket.setTicketId(document.getId()); // Set the ticket ID

                            Date eventDate = sdf.parse(ticket.getEventDate()); // Parse the event date

                            if (eventDate != null && !eventDate.before(today) && !ticket.isSold()) { // If the event date is in the future and the ticket is not sold
                                ticketList.add(ticket); // Add the ticket to the list
                            }
                        }
                    } catch (Exception e) { // If there is an error parsing the document
                        Log.e("FirestoreError", "Parsing error", e); // Log the error
                    }
                }
                sortTicketsByDate(); // Sort the tickets by date
                if (adapter != null) adapter.setFilteredList(new ArrayList<>(ticketList)); // Set the filtered list in the adapter
            }
        });
    }

    private void sortTicketsByDate() { // Sort the tickets by date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // Set up the date format
        Collections.sort(ticketList, (t1, t2) -> { // Sort the tickets by date
            try {
                Date d1 = sdf.parse(t1.getEventDate()); // Parse the event date from String to Date
                Date d2 = sdf.parse(t2.getEventDate()); // Parse the event date from String to Date
                return d1.compareTo(d2); // Compare the dates, return d1 if d1 is before d2, return d2 if d1 is after d2, return 0 if they are equal
            } catch (Exception e) { return 0; } // If there is an error parsing the date, return 0
        });
    }

    private void setupAdapter() {
        adapter = new TicketAdapter(ticketList, false, new TicketAdapter.OnTicketClickListener() { // Set up the adapter for the RecyclerView
            @Override
            public void onTicketClick(Ticket ticket) { // Handle click on a ticket
                TicketDetailsFragment detailsFragment = new TicketDetailsFragment(); // Create a new fragment instance
                Bundle args = new Bundle(); // Create a bundle to pass data to the fragment

                // Add the ticket ID to the bundle
                args.putString("ticketId", ticket.getTicketId());
                // Add other ticket details to the bundle
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

                detailsFragment.setArguments(args); // Set the arguments for the fragment
                getParentFragmentManager().beginTransaction() // Start a fragment transaction
                        .replace(R.id.main_container, detailsFragment)
                        .addToBackStack(null).commit(); // Replace the container with the TicketDetailsFragment and add the transaction to the back stack
            }
            @Override public void onEditClick(Ticket ticket) {}
            @Override public void onDeleteClick(Ticket ticket) {}
            @Override public void onSoldClick(Ticket ticket) {}
        });
        rvTicketList.setAdapter(adapter); // Set the adapter for the RecyclerView
    }

    private void filterList(String text) { // Filter the list of tickets based on the text
        if (adapter == null) return; // If the adapter is null, return
        List<Ticket> filteredList = new ArrayList<>(); // Create a new list to store the filtered tickets
        for (Ticket ticket : ticketList) { // Iterate over the list of tickets
            String name = (ticket.getEventName() != null) ? ticket.getEventName().toLowerCase() : ""; //Check if the ticket has the name field, if not, set it to empty
            String loc = (ticket.getLocation() != null) ? ticket.getLocation().toLowerCase() : ""; //Check if the ticket has the location field, if not, set it to empty
            String cat = (ticket.getCategory() != null) ? ticket.getCategory() : ""; // Check if the ticket has the category field, if not, set it to empty
            boolean matchesSearch = name.contains(text.toLowerCase()) || loc.contains(text.toLowerCase()); // Check if the name or location contains the text
            boolean matchesCategory = currentCategory.equals("הכל") || cat.equalsIgnoreCase(currentCategory); // Check if the category matches the current category
            if (matchesSearch && matchesCategory) filteredList.add(ticket); // If the ticket matches the search and the category, add it to the filtered list
        }
        adapter.setFilteredList(filteredList); // Set the filtered list in the adapter
    }

    private void setupCategoryButtons(View view) { // Set up the category buttons
        View.OnClickListener listener = v -> {
            currentCategory = ((Button) v).getText().toString(); // Set the current category to the text of the button
            filterList(searchView.getQuery().toString()); // Filter the list of tickets based on the current category
        };
        // Set up the listener for each category button
        int[] ids = {R.id.btnCatAll, R.id.btnCatConcert, R.id.btnCatSport, R.id.btnCatCinema, R.id.btnCatParties, R.id.btnCatOther};
        for (int id : ids) { // Iterate over the category buttons
            View btn = view.findViewById(id); // Find the button by ID
            if (btn != null) btn.setOnClickListener(listener); // Set the listener for the button
        }
    }
}