package com.safeticket.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
    private List<Ticket> ticketList;
    private SearchView searchView;
    private Spinner spinnerLocation; // New

    // Filter States
    private String currentCategory = "All";
    private String currentLocation = "All Areas"; // New

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
        spinnerLocation = view.findViewById(R.id.spinnerLocation); // New
        rvTicketList.setLayoutManager(new LinearLayoutManager(getContext()));

        createDummyData();

        // 1. Setup Adapter
        setupAdapter();

        // 2. Setup Location Spinner (New)
        setupLocationSpinner();

        // 3. Setup Search Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        // 4. Setup Category Buttons
        setupCategoryButtons(view);

        // 5. Handle "Add Ticket" Result
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
            ticketList.add(0, newTicket);
            filterList(searchView.getQuery().toString());
        });

        // 6. Handle Edit/Delete
        getParentFragmentManager().setFragmentResultListener("ticket_action_request", this, (requestKey, result) -> {
            String action = result.getString("action");
            String ticketId = result.getString("ticketId");

            if ("delete".equals(action)) {
                for (int i = 0; i < ticketList.size(); i++) {
                    if (ticketList.get(i).getTicketId().equals(ticketId)) {
                        ticketList.remove(i);
                        break;
                    }
                }
            } else if ("update_price".equals(action)) {
                double newPrice = result.getDouble("newPrice");
                for (int i = 0; i < ticketList.size(); i++) {
                    if (ticketList.get(i).getTicketId().equals(ticketId)) {
                        ticketList.get(i).setAskingPrice(newPrice);
                        break;
                    }
                }
            }
            filterList(searchView.getQuery().toString());
        });

        // 7. Profile & FAB
        view.findViewById(R.id.ivProfileButton).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.fabAddTicket).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new AddTicketFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    // --- Helper Methods ---

    private void setupLocationSpinner() {
        // Create list of locations
        String[] locations = {"All Areas", "Tel Aviv", "Haifa", "Jerusalem", "Eilat", "North", "South"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, locations);
        spinnerLocation.setAdapter(adapter);

        spinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentLocation = locations[position];
                filterList(searchView.getQuery().toString()); // Trigger filter
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

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

            // Dummy transparency data
            args.putFloat(TicketDetailsFragment.ARG_RATING, 4.5f);
            args.putBoolean(TicketDetailsFragment.ARG_VERIFIED, true);

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
            currentCategory = clickedBtn.getText().toString();
            filterList(searchView.getQuery().toString());
        };

        btnAll.setOnClickListener(categoryListener);
        btnConcert.setOnClickListener(categoryListener);
        btnSport.setOnClickListener(categoryListener);
        btnOther.setOnClickListener(categoryListener);
    }

    // The Smart Filter Logic 🧠
    private void filterList(String text) {
        List<Ticket> filteredList = new ArrayList<>();

        for (Ticket ticket : ticketList) {
            // 1. Check Search Text
            boolean matchesSearch = ticket.getEventName().toLowerCase().contains(text.toLowerCase()) ||
                    ticket.getLocation().toLowerCase().contains(text.toLowerCase());

            // 2. Check Category
            boolean matchesCategory = currentCategory.equals("All") ||
                    ticket.getCategory().equalsIgnoreCase(currentCategory);

            // 3. Check Location (New)
            boolean matchesLocation = currentLocation.equals("All Areas") ||
                    ticket.getLocation().contains(currentLocation);

            // ADD only if ALL conditions match
            if (matchesSearch && matchesCategory && matchesLocation) {
                filteredList.add(ticket);
            }
        }

        adapter.setFilteredList(filteredList);
    }

    private void createDummyData() {
        ticketList = new ArrayList<>();
        // Updated locations to match the filter options (Tel Aviv, Jerusalem, etc.)
        ticketList.add(new Ticket("101", "s1", "Coldplay Live", "Park Hayarkon, Tel Aviv", "Israel", System.currentTimeMillis(), 450.0, 600.0, "Concert", R.drawable.img_coldplay, "David Cohen", "050-1234567"));
        ticketList.add(new Ticket("102", "s2", "Football Match", "Bloomfield, Tel Aviv", "Israel", System.currentTimeMillis(), 120.0, 200.0, "Sport", R.drawable.img_football, "Yossi Levi", "052-9876543"));
        ticketList.add(new Ticket("103", "s3", "Omer Adam", "Menora Mivtachim, Tel Aviv", "Israel", System.currentTimeMillis(), 300.0, 450.0, "Concert", R.drawable.img_omeradam, "Dana Ron", "054-5555555"));
        ticketList.add(new Ticket("104", "s4", "Cinema City VIP", "Glilot, North", "Israel", System.currentTimeMillis(), 80.0, 150.0, "Other", R.drawable.img_cinema, "Roni Alon", "053-1112223"));
        ticketList.add(new Ticket("105", "s5", "Teddy Stadium", "Teddy, Jerusalem", "Israel", System.currentTimeMillis(), 100.0, 100.0, "Sport", R.drawable.ic_launcher_background, "Moshe", "050-1111111"));
    }
}