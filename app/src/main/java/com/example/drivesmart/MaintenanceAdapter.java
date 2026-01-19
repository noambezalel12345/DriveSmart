package com.example.drivesmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.MaintenanceViewHolder> {

    private final List<Maintenance> list;
    private final OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(Maintenance m); }

    public MaintenanceAdapter(List<Maintenance> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MaintenanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MaintenanceViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_maintenance, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MaintenanceViewHolder holder, int position) {
        Maintenance m = list.get(position);

        // הגדרת המספר הסידורי
        holder.tvNumber.setText(String.valueOf(position + 1));

        // הגדרת הכותרת
        holder.tvTitle.setText(m.title != null ? m.title : "ללא כותרת");

        // בדיקה בטוחה לתאריך - מונע קריסה אם dueDate הוא null
        if (m.dueDate != null && !m.dueDate.isEmpty()) {
            holder.tvDate.setText(m.dueDate);
            holder.tvDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvDate.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(m));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class MaintenanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvTitle, tvDate;
        public MaintenanceViewHolder(View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvTitle = itemView.findViewById(R.id.tvMaintenanceTitle);
            tvDate = itemView.findViewById(R.id.tvDeadline);
        }
    }
}