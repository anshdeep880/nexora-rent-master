package com.rentmaster.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rentmaster.app.R;
import com.rentmaster.app.data.entity.Room;
import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
    private List<Room> rooms = new ArrayList<>();
    private OnRoomInteractionListener listener;

    public interface OnRoomInteractionListener {
        void onRoomClick(Room room);

        void onEditClick(Room room);

        void onDeleteClick(Room room);
    }

    public RoomAdapter(OnRoomInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.tvRoomNumber.setText("Room: " + room.roomNumber);
        holder.tvRoomType.setText(room.roomType != null ? room.roomType : "Not specified");
        holder.tvRent.setText("Base Rent: $" + room.baseRent);
        holder.itemView.setOnClickListener(v -> listener.onRoomClick(room));
        holder.ivEdit.setOnClickListener(v -> listener.onEditClick(room));
        holder.ivDelete.setOnClickListener(v -> listener.onDeleteClick(room));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
        notifyDataSetChanged();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomNumber, tvRoomType, tvRent;
        ImageView ivEdit, ivDelete;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomNumber = itemView.findViewById(R.id.tv_room_number);
            tvRoomType = itemView.findViewById(R.id.tv_room_type);
            tvRent = itemView.findViewById(R.id.tv_room_rent);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
