package com.safeticket.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.safeticket.R;
import com.safeticket.adapter.TicketAdapter;
import com.safeticket.model.Ticket;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    // UI Components
    private RecyclerView rvTicketList;
    private TicketAdapter adapter;
    private List<Ticket> ticketList;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvTicketList = view.findViewById(R.id.rvTicketList);
        rvTicketList.setLayoutManager(new LinearLayoutManager(getContext()));

        createDummyData();

        // --- Add Listener here ---
        getParentFragmentManager().setFragmentResultListener("add_ticket_request", this, (requestKey, result) -> {
            // Get data from the AddTicketFragment
            String name = result.getString("name");
            String price = result.getString("price");
            String location = result.getString("location");
            String country = result.getString("country");

            // Create new ticket and add to list
            // (Currently using default image and static seller info until we progress)
            Ticket newTicket = new Ticket(name, location, country, price, R.drawable.ic_launcher_background, "Me", "050-0000000");

            ticketList.add(0, newTicket); // Add to the top of the list
            adapter.notifyItemInserted(0); // Update the display
            rvTicketList.scrollToPosition(0); // Scroll up to the new ticket
        });

        // Initialize Adapter with the Click Listener
        adapter = new TicketAdapter(ticketList, new TicketAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Ticket ticket) {
                TicketDetailsFragment detailsFragment = new TicketDetailsFragment();

                Bundle args = new Bundle();
                args.putString(TicketDetailsFragment.ARG_NAME, ticket.getEventName());
                args.putString(TicketDetailsFragment.ARG_PRICE, ticket.getPrice());
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
            }
        });

        rvTicketList.setAdapter(adapter);

        // Handle Floating Action Button (FAB) Click
        view.findViewById(R.id.fabAddTicket).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new AddTicketFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Set the adapter to the RecyclerView
        rvTicketList.setAdapter(adapter);

        // Handle Floating Action Button (FAB) Click
        view.findViewById(R.id.fabAddTicket).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new AddTicketFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void createDummyData() {
        ticketList = new ArrayList<>();

        // Coldplay -> img_coldplay
        ticketList.add(new Ticket("Coldplay Live", "Park Hayarkon, TLV", "Israel", "450 ₪", R.drawable.img_coldplay, "David Cohen", "050-1234567"));

        // Football -> img_football
        ticketList.add(new Ticket("Football Match", "Bloomfield Stadium", "Israel", "120 ₪", R.drawable.img_football, "Yossi Levi", "052-9876543"));

        // Omer Adam -> img_omeradam
        ticketList.add(new Ticket("Omer Adam", "Menora Mivtachim", "Israel", "300 ₪", R.drawable.img_omeradam, "Dana Ron", "054-5555555"));

        // Cinema -> img_cinema
        ticketList.add(new Ticket("Cinema City VIP", "Glilot", "Israel", "80 ₪", R.drawable.img_cinema, "Roni Alon", "053-1112223"));
    }
}