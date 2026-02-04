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

        // 1. Handle "Add Ticket" Result
        getParentFragmentManager().setFragmentResultListener("add_ticket_request", this, (requestKey, result) -> {
            String name = result.getString("name");
            String priceStr = result.getString("price");
            String location = result.getString("location");
            String country = result.getString("country");

            double priceValue = 0.0;
            try {
                priceValue = Double.parseDouble(priceStr.replace(" ₪", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }

            Ticket newTicket = new Ticket(
                    "999", "me", name, location, country,
                    System.currentTimeMillis(), priceValue, priceValue,
                    "General", R.drawable.ic_launcher_background, "Me", "050-0000000"
            );

            ticketList.add(0, newTicket);
            adapter.notifyItemInserted(0);
            rvTicketList.scrollToPosition(0);
        });

        // 2. Handle Profile Click
        view.findViewById(R.id.ivProfileButton).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 3. Initialize Adapter
        adapter = new TicketAdapter(ticketList, new TicketAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Ticket ticket) {
                TicketDetailsFragment detailsFragment = new TicketDetailsFragment();

                Bundle args = new Bundle();
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
            }
        });

        // 4. Set Adapter & FAB
        rvTicketList.setAdapter(adapter);

        view.findViewById(R.id.fabAddTicket).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new AddTicketFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void createDummyData() {
        ticketList = new ArrayList<>();

        // Updated Constructor usage:
        // ID, SellerID, Name, Location, Country, Date, AskingPrice, OriginalPrice, Category, Image, SellerName, Phone

        ticketList.add(new Ticket("101", "s1", "Coldplay Live", "Park Hayarkon, TLV", "Israel", System.currentTimeMillis(), 450.0, 600.0, "Concert", R.drawable.img_coldplay, "David Cohen", "050-1234567"));
        ticketList.add(new Ticket("102", "s2", "Football Match", "Bloomfield Stadium", "Israel", System.currentTimeMillis(), 120.0, 200.0, "Sport", R.drawable.img_football, "Yossi Levi", "052-9876543"));
        ticketList.add(new Ticket("103", "s3", "Omer Adam", "Menora Mivtachim", "Israel", System.currentTimeMillis(), 300.0, 450.0, "Concert", R.drawable.img_omeradam, "Dana Ron", "054-5555555"));
        ticketList.add(new Ticket("104", "s4", "Cinema City VIP", "Glilot", "Israel", System.currentTimeMillis(), 80.0, 150.0, "Movie", R.drawable.img_cinema, "Roni Alon", "053-1112223"));
    }
}