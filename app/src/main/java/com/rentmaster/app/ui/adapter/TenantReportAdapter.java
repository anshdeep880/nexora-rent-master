package com.rentmaster.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rentmaster.app.R;
import com.rentmaster.app.data.model.TenantReport;
import com.rentmaster.app.util.FormatUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TenantReportAdapter extends RecyclerView.Adapter<TenantReportAdapter.TenantReportViewHolder> {
    private List<TenantReport> tenantReports = new ArrayList<>();

    public void setTenantReports(List<TenantReport> tenantReports) {
        this.tenantReports = tenantReports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TenantReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tenant_report, parent, false);
        return new TenantReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TenantReportViewHolder holder, int position) {
        TenantReport report = tenantReports.get(position);
        holder.tvTenantName.setText(report.getTenantName());
        holder.tvRoomNumber.setText("Room: " + (report.getRoomNumber() != null ? report.getRoomNumber() : "N/A"));
        holder.tvTotalPaid.setText("Total Paid: " + FormatUtils.formatCurrency(report.getTotalPaid()));
    }

    @Override
    public int getItemCount() {
        return tenantReports.size();
    }

    static class TenantReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenantName, tvRoomNumber, tvTotalPaid;

        public TenantReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenantName = itemView.findViewById(R.id.tv_tenant_name);
            tvRoomNumber = itemView.findViewById(R.id.tv_room_number);
            tvTotalPaid = itemView.findViewById(R.id.tv_total_paid);
        }
    }
}
