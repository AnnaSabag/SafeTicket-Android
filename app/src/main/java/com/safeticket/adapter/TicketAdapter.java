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

        // התאמה למודל שלך
        holder.tvEventName.setText(ticket.getEventName());
        holder.tvPrice.setText(ticket.getAskingPrice() + " ₪");
        holder.tvLocation.setText(ticket.getLocation());

        // ניהול כפתורי עריכה/מחיקה במצב פרופיל
        if (isProfileMode) {
            holder.layoutActions.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> listener.onEditClick(ticket));
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(ticket));
        } else {
            holder.layoutActions.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onTicketClick(ticket));

        // פענוח תמונה
        String base64Image = ticket.getTicketImage();
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivTicket.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.ivTicket.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }

    @Override
    public int getItemCount() {
        return ticketList != null ? ticketList.size() : 0;
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvPrice, tvLocation;
        ImageView ivTicket;
        LinearLayout layoutActions;
        ImageButton btnEdit, btnDelete;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            ivTicket = itemView.findViewById(R.id.ivTicketImage);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnEdit = itemView.findViewById(R.id.btnEditTicket);
            btnDelete = itemView.findViewById(R.id.btnDeleteTicket);
        }
    }
}