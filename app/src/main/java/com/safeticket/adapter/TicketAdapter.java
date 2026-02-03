package com.safeticket.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.safeticket.R;
import com.safeticket.model.Ticket;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Ticket> ticketList;
    private OnItemClickListener listener; // The listener reference

    // Interface that defines what happens when an item is clicked
    public interface OnItemClickListener {
        void onItemClick(Ticket ticket);
    }

    // Constructor: receives the data list and the listener
    public TicketAdapter(List<Ticket> ticketList, OnItemClickListener listener) {
        this.ticketList = ticketList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);

        // Set data to views
        holder.tvEventName.setText(ticket.getEventName());
        holder.tvLocation.setText(ticket.getLocation());
        holder.tvLocation.setText(ticket.getLocation() + ", " + ticket.getCountry());
        holder.tvPrice.setText(ticket.getPrice());
        holder.ivTicketImage.setImageResource(ticket.getEventImage());

        // Handle item click: trigger the listener
        holder.itemView.setOnClickListener(v -> listener.onItemClick(ticket));

    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    // ViewHolder inner class
    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvLocation, tvPrice;
        ImageView ivTicketImage;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivTicketImage = itemView.findViewById(R.id.ivTicketImage);
        }
    }
}