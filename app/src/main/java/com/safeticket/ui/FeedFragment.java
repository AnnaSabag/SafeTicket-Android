package com.safeticket.ui;

import android.os.Bundle;
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
import com.safeticket.R;
import com.safeticket.adapter.TicketAdapter;
import com.safeticket.model.Ticket;
import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private RecyclerView rvTicketList;
    private TicketAdapter adapter;
    private List<Ticket> ticketList; // Holds ALL tickets (Original source)
    private SearchView searchView;
    private String currentCategory = "All"; // To keep track of filter

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        rvTicketList = view.findViewById(R.id.rvTicketList);
        searchView = view.findViewById(R.id.searchView);
        rvTicketList.setLayoutManager(new LinearLayoutManager(getContext()));

        createDummyData();

        // 1. Setup Adapter
        setupAdapter();

        // 2. Setup Search Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText); // Call filter every time text changes
                return true;
            }
        });

        // 3. Setup Category Buttons
        setupCategoryButtons(view);

        // 4. Handle "Add Ticket" Result
        getParentFragmentManager().setFragmentResultListener("add_ticket_request", this, (requestKey, result) -> {
            String name = result.getString("name");
            String priceStr = result.getString("price");
            String location = result.getString("location");
            String country = result.getString("country");
            String category = result.getString("category");

            double priceValue = 0.0;
            try {
                priceValue = Double.parseDouble(priceStr.replace(" ₪", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }

            Ticket newTicket = new Ticket(
                    "999", "me", name, location, country,
                    System.currentTimeMillis(), priceValue, priceValue,
                    category, R.drawable.ic_launcher_background, "Me", "050-0000000"
            );

            // Add to the ORIGINAL list
            ticketList.add(0, newTicket);
            // Refresh the view
            filterList(searchView.getQuery().toString());
        });

        // --- 5. NEW: Handle Edit/Delete Actions (from Details Screen) ---
        getParentFragmentManager().setFragmentResultListener("ticket_action_request", this, (requestKey, result) -> {
            String action = result.getString("action");
            String ticketId = result.getString("ticketId");

            if ("delete".equals(action)) {
                // Find and remove ticket by ID
                for (int i = 0; i < ticketList.size(); i++) {
                    if (ticketList.get(i).getTicketId().equals(ticketId)) {
                        ticketList.remove(i);
                        break;
                    }
                }
            } else if ("update_price".equals(action)) {
                // Find and update price by ID
                double newPrice = result.getDouble("newPrice");
                for (int i = 0; i < ticketList.size(); i++) {
                    if (ticketList.get(i).getTicketId().equals(ticketId)) {
                        ticketList.get(i).setAskingPrice(newPrice);
                        break;
                    }
                }
            }
            // Refresh the list to show changes
            filterList(searchView.getQuery().toString());
        });

        // 6. Handle Profile Click
        view.findViewById(R.id.ivProfileButton).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 7. Handle FAB
        view.findViewById(R.id.fabAddTicket).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new AddTicketFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    // --- Helper Methods ---

    private void setupAdapter() {
        adapter = new TicketAdapter(ticketList, ticket -> {
            TicketDetailsFragment detailsFragment = new TicketDetailsFragment();
            Bundle args = new Bundle();
            args.putString(TicketDetailsFragment.ARG_ID, ticket.getTicketId());
            args.putString(TicketDetailsFragment.ARG_NAME, ticket.getEventName());
            args.putString(TicketDetailsFragment.ARG_PRICE, String.valueOf(ticket.getAskingPrice()) + " ₪");
            args.putString(TicketDetailsFragment.ARG_LOCATION, ticket.getLocation());
            args.putString(TicketDetailsFragment.ARG_COUNTRY, ticket.getCountry());
            args.putString(TicketDetailsFragment.ARG_SELLER, ticket.getSellerName());
            args.putString(TicketDetailsFragment.ARG_PHONE, ticket.getSellerPhone());
            args.putInt(TicketDetailsFragment.ARG_IMAGE, ticket.getEventImage());
            detailsFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, detailsFragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvTicketList.setAdapter(adapter);
    }

    private void setupCategoryButtons(View view) {
        Button btnAll = view.findViewById(R.id.btnCatAll);
        Button btnConcert = view.findViewById(R.id.btnCatConcert);
        Button btnSport = view.findViewById(R.id.btnCatSport);
        Button btnOther = view.findViewById(R.id.btnCatOther);

        View.OnClickListener categoryListener = v -> {
            Button clickedBtn = (Button) v;
            currentCategory = clickedBtn.getText().toString(); // Update current category
            filterList(searchView.getQuery().toString()); // Filter with current search text
        };

        btnAll.setOnClickListener(categoryListener);
        btnConcert.setOnClickListener(categoryListener);
        btnSport.setOnClickListener(categoryListener);
        btnOther.setOnClickListener(categoryListener);
    }

    // The Filter Logic 🧠
    private void filterList(String text) {
        List<Ticket> filteredList = new ArrayList<>();

        for (Ticket ticket : ticketList) {
            // Check 1: Does the name match the search text? (Case insensitive)
            boolean matchesSearch = ticket.getEventName().toLowerCase().contains(text.toLowerCase()) ||
                    ticket.getLocation().toLowerCase().contains(text.toLowerCase());

            // Check 2: Does the category match? (Or is "All" selected?)
            boolean matchesCategory = currentCategory.equals("All") ||
                    ticket.getCategory().equalsIgnoreCase(currentCategory);

            // Add only if BOTH match
            if (matchesSearch && matchesCategory) {
                filteredList.add(ticket);
            }
        }

        if (filteredList.isEmpty()) {
            // Optional: Show a "No results found" Toast
        }

        adapter.setFilteredList(filteredList);
    }

    private void createDummyData() {
        ticketList = new ArrayList<>();
        ticketList.add(new Ticket("101", "s1", "Coldplay Live", "Park Hayarkon, TLV", "Israel", System.currentTimeMillis(), 450.0, 600.0, "Concert", R.drawable.img_coldplay, "David Cohen", "050-1234567"));
        ticketList.add(new Ticket("102", "s2", "Football Match", "Bloomfield Stadium", "Israel", System.currentTimeMillis(), 120.0, 200.0, "Sport", R.drawable.img_football, "Yossi Levi", "052-9876543"));
        ticketList.add(new Ticket("103", "s3", "Omer Adam", "Menora Mivtachim", "Israel", System.currentTimeMillis(), 300.0, 450.0, "Concert", R.drawable.img_omeradam, "Dana Ron", "054-5555555"));
        ticketList.add(new Ticket("104", "s4", "Cinema City VIP", "Glilot", "Israel", System.currentTimeMillis(), 80.0, 150.0, "Other", R.drawable.img_cinema, "Roni Alon", "053-1112223"));
    }
}