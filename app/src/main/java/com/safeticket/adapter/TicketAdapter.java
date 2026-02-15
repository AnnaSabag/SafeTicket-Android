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

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Ticket> ticketList;
    private OnTicketClickListener listener;
    private boolean isProfileMode;

    public interface OnTicketClickListener {
        void onTicketClick(Ticket ticket);
        void onEditClick(Ticket ticket);
        void onDeleteClick(Ticket ticket);
        void onSoldClick(Ticket ticket);
    }

    public TicketAdapter(List<Ticket> ticketList, boolean isProfileMode, OnTicketClickListener listener) {
        this.ticketList = ticketList;
        this.isProfileMode = isProfileMode;
        this.listener = listener;
    }

    public void setFilteredList(List<Ticket> filteredList) {
        this.ticketList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);

        holder.tvEventName.setText(ticket.getEventName());
        holder.tvPrice.setText(ticket.getAskingPrice() + " ₪");
        holder.tvCategory.setText(ticket.getCategory());

        if (ticket.getQuantity() > 1) {
            holder.tvCardQuantity.setVisibility(View.VISIBLE);
            holder.tvCardQuantity.setText("x" + ticket.getQuantity());
        } else {
            holder.tvCardQuantity.setVisibility(View.GONE);
        }

        String fullAddress = ticket.getLocation();
        if (ticket.getExactAddress() != null && !ticket.getExactAddress().isEmpty()) {
            String fullLocation = fullAddress + " | " + ticket.getExactAddress();
            holder.tvLocation.setText(fullLocation);
        } else {
            holder.tvLocation.setText(fullAddress);
        }

        holder.tvDateTime.setText(ticket.getEventDate() + " • " + ticket.getEventTime());

        if (isProfileMode) {
            holder.layoutActions.setVisibility(View.VISIBLE);
            holder.layoutViews.setVisibility(View.VISIBLE); // הצגת צפיות בפרופיל
            holder.tvViewCount.setText(String.valueOf(ticket.getViewCount()));

            holder.btnMarkAsSold.setOnClickListener(v -> listener.onSoldClick(ticket));
            holder.btnEdit.setOnClickListener(v -> listener.onEditClick(ticket));
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(ticket));
        } else {
            holder.layoutActions.setVisibility(View.GONE);
            holder.layoutViews.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onTicketClick(ticket));

        String img = ticket.getTicketImage();
        if (img != null && !img.isEmpty()) {
            try {
                byte[] decoded = Base64.decode(img, Base64.DEFAULT);
                holder.ivTicket.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.length));
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @Override
    public int getItemCount() { return ticketList != null ? ticketList.size() : 0; }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvPrice, tvLocation, tvDateTime, tvCategory, tvCardQuantity, tvViewCount;
        ImageView ivTicket;
        LinearLayout layoutActions, layoutViews;
        ImageButton btnEdit, btnDelete, btnMarkAsSold;

        public TicketViewHolder(@NonNull View itemView) {
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