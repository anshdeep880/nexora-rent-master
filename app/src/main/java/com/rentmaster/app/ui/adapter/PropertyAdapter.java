package com.rentmaster.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rentmaster.app.R;
import com.rentmaster.app.data.entity.Property;
import com.rentmaster.app.data.model.PropertyWithRoomCount;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {
    private List<PropertyWithRoomCount> properties = new ArrayList<>();
    private OnPropertyInteractionListener listener;

    public interface OnPropertyInteractionListener {
        void onPropertyClick(Property property);
        void onEditClick(Property property);
        void onDeleteClick(Property property);
    }

    public PropertyAdapter(OnPropertyInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_property, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        PropertyWithRoomCount item = properties.get(position);
        Property property = item.property;
        holder.tvName.setText(property.name);
        holder.tvAddress.setText(property.address);
        
        String roomInfo = item.totalRooms + " Rooms • " + item.occupiedRooms + " Occupied";
        holder.chipRoomCount.setText(roomInfo);
        
        holder.itemView.setOnClickListener(v -> listener.onPropertyClick(property));
        holder.ivEdit.setOnClickListener(v -> listener.onEditClick(property));
        holder.ivDelete.setOnClickListener(v -> listener.onDeleteClick(property));
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    public void setProperties(List<PropertyWithRoomCount> properties) {
        this.properties = properties;
        notifyDataSetChanged();
    }

    static class PropertyViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress;
        Chip chipRoomCount;
        ImageView ivEdit, ivDelete;
        PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_property_name);
            tvAddress = itemView.findViewById(R.id.tv_property_address);
            chipRoomCount = itemView.findViewById(R.id.tv_room_count);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
