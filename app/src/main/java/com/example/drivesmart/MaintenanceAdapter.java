package com.example.drivesmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.MaintenanceViewHolder> {

    private final List<Maintenance> list;
    private final List<Maintenance> selectedItems = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(Maintenance m); }

    public MaintenanceAdapter(List<Maintenance> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public List<Maintenance> getSelectedItems() { return selectedItems; }

    @NonNull
    @Override
    public MaintenanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_maintenance, parent, false);
        return new MaintenanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaintenanceViewHolder holder, int position) {
        Maintenance m = list.get(position);
        holder.tvNumber.setText(String.valueOf(position + 1));
        holder.tvTitle.setText(m.title);
        holder.tvDate.setText(m.dueDate);

        // ניקוי מצב קודם של הצ'קבוקס
        holder.cbDone.setOnCheckedChangeListener(null);
        holder.cbDone.setChecked(selectedItems.contains(m));

        // ניהול רשימת המסומנים
        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedItems.add(m);
            else selectedItems.remove(m);
        });

        holder.itemView.setOnClickListener(v -> listener.onItemClick(m));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class MaintenanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvTitle, tvDate;
        CheckBox cbDone;
        public MaintenanceViewHolder(View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvTitle = itemView.findViewById(R.id.tvMaintenanceTitle);
            tvDate = itemView.findViewById(R.id.tvDeadline);
            cbDone = itemView.findViewById(R.id.cbDone);
        }
    }
}