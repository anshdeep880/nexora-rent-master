package com.rentmaster.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rentmaster.app.R;
import com.rentmaster.app.data.entity.Tenant;
import com.rentmaster.app.util.FormatUtils;
import java.util.ArrayList;
import java.util.List;

public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.TenantViewHolder> {
    private List<Tenant> tenants = new ArrayList<>();
    private List<Tenant> filteredTenants = new ArrayList<>();
    private OnTenantInteractionListener listener;

    public interface OnTenantInteractionListener {
        void onTenantClick(Tenant tenant);

        void onEditClick(Tenant tenant);

        void onDeleteClick(Tenant tenant);
    }

    public TenantAdapter(OnTenantInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TenantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tenant, parent, false);
        return new TenantViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TenantViewHolder holder, int position) {
        Tenant tenant = filteredTenants.get(position);
        holder.tvName.setText(tenant.name);
        holder.tvPhone.setText("Phone: " + tenant.phone);

        holder.tvFrequency.setText("Pay: " + (tenant.rentFrequency != null ? tenant.rentFrequency : "Monthly"));

        if (tenant.startDate > 0) {
            holder.tvStartDate.setText("Started: " + FormatUtils.formatDateWithMonth(tenant.startDate));
            holder.tvStartDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvStartDate.setVisibility(View.GONE);
        }

        holder.tvRoom.setText("Status: " + (tenant.isActive ? "Active" : "Vacated"));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null)
                listener.onTenantClick(tenant);
        });
        holder.ivEdit.setOnClickListener(v -> listener.onEditClick(tenant));
        holder.ivDelete.setOnClickListener(v -> listener.onDeleteClick(tenant));
    }

    @Override
    public int getItemCount() {
        return filteredTenants.size();
    }

    public void setTenants(List<Tenant> tenants) {
        this.tenants = tenants;
        this.filteredTenants = new ArrayList<>(tenants);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredTenants.clear();
        if (query.isEmpty()) {
            filteredTenants.addAll(tenants);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Tenant tenant : tenants) {
                if (tenant.name.toLowerCase().contains(lowerQuery)) {
                    filteredTenants.add(tenant);
                }
            }
        }
        notifyDataSetChanged();
    }

    class TenantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvRoom, tvFrequency, tvStartDate;
        ImageView ivEdit, ivDelete;

        public TenantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_tenant_name);
            tvPhone = itemView.findViewById(R.id.tv_tenant_phone);
            tvFrequency = itemView.findViewById(R.id.tv_rent_frequency);
            tvStartDate = itemView.findViewById(R.id.tv_start_date);
            tvRoom = itemView.findViewById(R.id.tv_room_info);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
