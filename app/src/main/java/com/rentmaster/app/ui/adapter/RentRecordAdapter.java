package com.rentmaster.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rentmaster.app.R;
import com.rentmaster.app.data.entity.RentRecordWithDetails;
import com.rentmaster.app.util.FormatUtils;
import com.google.android.material.chip.Chip;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RentRecordAdapter extends RecyclerView.Adapter<RentRecordAdapter.RentViewHolder> {
    private final List<RentRecordWithDetails> allRecords = new ArrayList<>();
    private final List<RentRecordWithDetails> filteredRecords = new ArrayList<>();
    private final OnRecordClickListener listener;
    private boolean showEditButton = true;
    private String currentSearchQuery = "";
    private int filterPropertyId = -1;
    private int filterRoomId = -1;

    public interface OnRecordClickListener {
        void onRecordClick(RentRecordWithDetails record);

        void onEditClick(RentRecordWithDetails record);
        void onDeleteClick(RentRecordWithDetails record);
    }

    public RentRecordAdapter(OnRecordClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rent_record, parent, false);
        return new RentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RentViewHolder holder, int position) {
        RentRecordWithDetails record = filteredRecords.get(position);

        holder.tvTenantName.setText(record.tenantName);
        holder.tvRoomNumber.setText(String.format("Room: %s", record.roomNumber));

        if (!record.isActive) {
            holder.tvVacatedStatus.setVisibility(View.VISIBLE);
        } else {
            holder.tvVacatedStatus.setVisibility(View.GONE);
        }

        String periodStart = FormatUtils.formatDateWithMonth(record.rentRecord.periodStart);
        String periodEnd = FormatUtils.formatDateWithMonth(record.rentRecord.periodEnd);
        holder.tvPeriod.setText(String.format(Locale.getDefault(), "%s - %s", periodStart, periodEnd));

        double balance = Math.max(0, record.rentRecord.amountDue - record.rentRecord.amountPaid);
        holder.tvAmount.setText(String.format(Locale.getDefault(), "Rent: %s | Paid: %s | Due: %s",
                FormatUtils.formatCurrency(record.rentRecord.amountDue),
                FormatUtils.formatCurrency(record.rentRecord.amountPaid),
                FormatUtils.formatCurrency(balance)));
        holder.chipStatus.setText(record.rentRecord.status);

        if (record.rentRecord.amountPaid > record.rentRecord.amountDue) {
            holder.chipOverpaid.setVisibility(View.VISIBLE);
        } else {
            holder.chipOverpaid.setVisibility(View.GONE);
        }

        int color = 0xFF616161; // Gray
        if ("Paid".equals(record.rentRecord.status))
            color = 0xFF2E7D32; // Green
        else if ("Partial".equals(record.rentRecord.status))
            color = 0xFFF57C00; // Orange
        else if ("Unpaid".equals(record.rentRecord.status))
            color = 0xFFC62828; // Red
        else if ("Pending".equals(record.rentRecord.status))
            color = 0xFF1976D2; // Blue
        holder.chipStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(color));
        holder.itemView.setOnClickListener(v -> listener.onRecordClick(record));

        if (showEditButton) {
            holder.ivEdit.setVisibility(View.VISIBLE);
            holder.ivDelete.setVisibility(View.VISIBLE);
            holder.ivEdit.setOnClickListener(v -> listener.onEditClick(record));
            holder.ivDelete.setOnClickListener(v -> listener.onDeleteClick(record));
        } else {
            holder.ivEdit.setVisibility(View.GONE);
            holder.ivDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return filteredRecords.size();
    }

    public void setRecords(List<RentRecordWithDetails> records) {
        this.allRecords.clear();
        this.allRecords.addAll(records);
        applyFilters();
    }

    public void setShowEditButton(boolean showEditButton) {
        this.showEditButton = showEditButton;
    }

    public void filter(String query) {
        this.currentSearchQuery = query;
        applyFilters();
    }

    public void setFilters(int propertyId, int roomId) {
        this.filterPropertyId = propertyId;
        this.filterRoomId = roomId;
        applyFilters();
    }

    private void applyFilters() {
        filteredRecords.clear();
        String query = currentSearchQuery.toLowerCase().trim();

        for (RentRecordWithDetails record : allRecords) {
            boolean matchesSearch = query.isEmpty() ||
                    record.tenantName.toLowerCase().contains(query) ||
                    record.roomNumber.toLowerCase().contains(query);

            boolean matchesProperty = filterPropertyId == -1 || record.propertyId == filterPropertyId;
            boolean matchesRoom = filterRoomId == -1 || record.rentRecord.roomId == filterRoomId;

            if (matchesSearch && matchesProperty && matchesRoom) {
                filteredRecords.add(record);
            }
        }
        notifyDataSetChanged();
    }

    static class RentViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenantName, tvRoomNumber, tvPeriod, tvAmount, tvVacatedStatus;
        Chip chipStatus, chipOverpaid;
        android.widget.ImageView ivEdit, ivDelete;

        RentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenantName = itemView.findViewById(R.id.tv_tenant_name);
            tvRoomNumber = itemView.findViewById(R.id.tv_room_number);
            tvPeriod = itemView.findViewById(R.id.tv_rent_period);
            tvAmount = itemView.findViewById(R.id.tv_rent_amount);
            tvVacatedStatus = itemView.findViewById(R.id.tv_vacated_status);
            chipStatus = itemView.findViewById(R.id.tv_rent_status);
            chipOverpaid = itemView.findViewById(R.id.tv_overpaid_tag);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
