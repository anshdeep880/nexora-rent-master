package com.rentmaster.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rentmaster.app.R;
import com.rentmaster.app.data.entity.Room;
import com.rentmaster.app.ui.adapter.RoomAdapter;
import com.rentmaster.app.ui.viewmodel.RoomViewModel;
import com.google.android.material.button.MaterialButton;

public class PropertyDetailActivity extends AppCompatActivity implements RoomAdapter.OnRoomInteractionListener {
    private RoomViewModel roomViewModel;
    private RoomAdapter adapter;
    private int propertyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Property Details");
        }

        propertyId = getIntent().getIntExtra("PROPERTY_ID", -1);
        if (propertyId == -1) {
            finish();
            return;
        }


        RecyclerView recyclerView = findViewById(R.id.rv_rooms);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoomAdapter(this);
        recyclerView.setAdapter(adapter);

        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        roomViewModel.getRoomsForProperty(propertyId).observe(this, rooms -> {
            adapter.setRooms(rooms);
        });

        MaterialButton btnAddRoom = findViewById(R.id.btn_add_room);
        btnAddRoom.setOnClickListener(v -> {
            Intent intent = new Intent(PropertyDetailActivity.this, AddRoomActivity.class);
            intent.putExtra("PROPERTY_ID", propertyId);
            startActivity(intent);
        });

        MaterialButton btnExpenses = findViewById(R.id.btn_view_expenses);
        btnExpenses.setOnClickListener(v -> {
            Intent intent = new Intent(PropertyDetailActivity.this, ExpenseActivity.class);
            intent.putExtra("PROPERTY_ID", propertyId);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_property_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_edit) {
            Intent intent = new Intent(this, AddPropertyActivity.class);
            intent.putExtra("PROPERTY_ID", propertyId);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Property")
                .setMessage("Are you sure you want to delete this property? All associated rooms and data will be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    com.rentmaster.app.data.entity.Property property = new com.rentmaster.app.data.entity.Property(null, null, null);
                    property.id = propertyId;
                    new com.rentmaster.app.ui.viewmodel.PropertyViewModel(getApplication()).delete(property);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRoomClick(Room room) {
        Intent intent = new Intent(PropertyDetailActivity.this, RoomDetailActivity.class);
        intent.putExtra("ROOM_ID", room.id);
        startActivity(intent);
    }

    @Override
    public void onEditClick(Room room) {
        Intent intent = new Intent(this, AddRoomActivity.class);
        intent.putExtra("ROOM_ID", room.id);
        intent.putExtra("PROPERTY_ID", room.propertyId);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Room room) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Room")
            .setMessage("Are you sure you want to delete this room?")
            .setPositiveButton("Delete", (dialog, which) -> roomViewModel.delete(room))
            .setNegativeButton("Cancel", null)
            .show();
    }
}
