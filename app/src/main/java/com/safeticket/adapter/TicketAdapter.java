package com.safeticket.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.safeticket.R;
import com.safeticket.model.Ticket;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> { // Declare the adapter for the ticket items


    private List<Ticket> ticketList; // Declare the list of tickets, which will be displayed in the RecyclerView
    private OnTicketClickListener listener; // Declare the listener for ticket clicks
    private boolean isProfileMode; // Declare a flag to indicate if the adapter is in profile mode or in feed mode

    public interface OnTicketClickListener { // Declare the interface for ticket clicks
        void onTicketClick(Ticket ticket); // Method to handle ticket clicks
        void onEditClick(Ticket ticket); // Method to handle edit ticket clicks
        void onDeleteClick(Ticket ticket); // Method to handle delete ticket clicks
        void onSoldClick(Ticket ticket); // Method to handle sold ticket clicks
    }

    public TicketAdapter(List<Ticket> ticketList, boolean isProfileMode, OnTicketClickListener listener) { // Constructor
        this.ticketList = ticketList;
        this.isProfileMode = isProfileMode;
        this.listener = listener;
    }

    public void setFilteredList(List<Ticket> filteredList) { // Method to set the filtered list of tickets - works only in feed mode
        this.ticketList = filteredList; // Update the list of tickets
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false); // Create a new view
        return new TicketViewHolder(view); // Return the view holder
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) { // Bind the data to the view holder
        Ticket ticket = ticketList.get(position); // Get the ticket at the current position

        holder.tvEventName.setText(ticket.getEventName()); // Set the text for the event name
        holder.tvPrice.setText(ticket.getAskingPrice() + " ₪"); // Set the text for the price
        holder.tvCategory.setText(ticket.getCategory()); // Set the text for the category


        if (ticket.getQuantity() > 1) { // If the ticket has a quantity, set the text for the quantity
            holder.tvCardQuantity.setVisibility(View.VISIBLE); // Show the quantity
            holder.tvCardQuantity.setText("x" + ticket.getQuantity()); // Set the text for the quantity
        } else {
            holder.tvCardQuantity.setVisibility(View.GONE); // Hide the quantity, no need to show it
        }

        String fullAddress = ticket.getLocation(); // Get the full address of the ticket

        if (ticket.getExactAddress() != null && !ticket.getExactAddress().isEmpty()) { // If the ticket has an exact address, add it to the full address
            String fullLocation = fullAddress + " | " + ticket.getExactAddress(); // Add the exact address to the full address
            holder.tvLocation.setText(fullLocation); // Set the text for the location
        } else { // If the ticket has no exact address, set the full address as the text for the location
            holder.tvLocation.setText(fullAddress); // Set the text for the location
        }

        holder.tvDateTime.setText(ticket.getEventDate() + " | " + ticket.getEventTime()); // Set the text for the date and time

        if (isProfileMode) { // If the adapter is in profile mode, show the actions and views
            holder.layoutActions.setVisibility(View.VISIBLE); // Set the visibility of the actions (delete, edit, mark as sold)
            holder.layoutViews.setVisibility(View.VISIBLE); // Set the visibility of the views (view count)
            holder.tvViewCount.setText(String.valueOf(ticket.getViewCount())); // Set the text for the view count

            holder.btnMarkAsSold.setOnClickListener(v -> listener.onSoldClick(ticket)); // Set the click listener for the mark as sold button
            holder.btnEdit.setOnClickListener(v -> listener.onEditClick(ticket)); // Set the click listener for the edit button
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(ticket)); // Set the click listener for the delete button
        } else {
            holder.layoutActions.setVisibility(View.GONE); // If the adapter is in feed mode, hide the actions
            holder.layoutViews.setVisibility(View.GONE); // If the adapter is in feed mode, hide the views
        }

        holder.itemView.setOnClickListener(v -> listener.onTicketClick(ticket)); // Set the click listener for the item view, on click call the onTicketClick method


        String img = ticket.getTicketImage(); // Get the image of the ticket
        if (img != null && !img.isEmpty()) { // If the ticket has an image, decode it and set it as the image for the ImageView
            try {
                byte[] decoded = Base64.decode(img, Base64.DEFAULT); // Decode the image from base64 to a byte array because the image is stored as a string in the database to save space in the database
                holder.ivTicket.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.length)); // Decode the byte array to a Bitmap and set it as the image for the ImageView
            } catch (Exception e) { e.printStackTrace(); } // If there is an error decoding the image, print the stack trace
        }
    }

    @Override
    public int getItemCount() { return ticketList != null ? ticketList.size() : 0; } // Return the number of items in the list to know how many items to display in the RecyclerView


    public static class TicketViewHolder extends RecyclerView.ViewHolder { // Declare the view holder for the ticket items
        TextView tvEventName, tvPrice, tvLocation, tvDateTime, tvCategory, tvCardQuantity, tvViewCount; // Declare the text views for the ticket items
        ImageView ivTicket; // Declare the image view for the ticket image
        LinearLayout layoutActions, layoutViews; // Declare the linear layouts for the actions and views
        ImageButton btnEdit, btnDelete, btnMarkAsSold; // Declare the image buttons for the actions


        public TicketViewHolder(@NonNull View itemView) { // Constructor for the view holder in the ticket items in the RecyclerView at the profile fragment
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvCategory = itemView.findViewById(R.id.tvCardCategory);
            tvCardQuantity = itemView.findViewById(R.id.tvCardQuantity);
            tvViewCount = itemView.findViewById(R.id.tvViewCount);
            ivTicket = itemView.findViewById(R.id.ivTicketImage);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            layoutViews = itemView.findViewById(R.id.layoutViews);
            btnEdit = itemView.findViewById(R.id.btnEditTicket);
            btnDelete = itemView.findViewById(R.id.btnDeleteTicket);
            btnMarkAsSold = itemView.findViewById(R.id.btnMarkAsSold);
        }
    }
}