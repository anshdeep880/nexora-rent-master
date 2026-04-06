package com.rentmaster.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rentmaster.app.R;
import com.rentmaster.app.data.model.RoomReport;
import com.rentmaster.app.util.FormatUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoomReportAdapter extends RecyclerView.Adapter<RoomReportAdapter.RoomReportViewHolder> {
    private List<RoomReport> roomReports = new ArrayList<>();

    public void setRoomReports(List<RoomReport> roomReports) {
        this.roomReports = roomReports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_report, parent, false);
        return new RoomReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomReportViewHolder holder, int position) {
        RoomReport report = roomReports.get(position);
        holder.tvRoomNumber.setText("Room " + report.getRoomNumber());
        holder.tvTenantName.setText("Tenant: " + (report.getTenantName() != null ? report.getTenantName() : "N/A"));
        holder.tvOutstandingRent.setText("Outstanding: " + FormatUtils.formatCurrency(report.getOutstandingRent()));
    }

    @Override
    public int getItemCount() {
        return roomReports.size();
    }

    static class RoomReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomNumber, tvTenantName, tvOutstandingRent;

        public RoomReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomNumber = itemView.findViewById(R.id.tv_room_number);
            tvTenantName = itemView.findViewById(R.id.tv_tenant_name);
            tvOutstandingRent = itemView.findViewById(R.id.tv_outstanding_rent);
        }
    }
}
