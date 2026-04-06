package com.rentmaster.app.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rentmaster.app.util.CalculatorDialog;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.rentmaster.app.R;
import com.rentmaster.app.data.entity.Room;
import com.rentmaster.app.ui.viewmodel.RoomViewModel;

public class AddRoomActivity extends AppCompatActivity {
    private RoomViewModel roomViewModel;
    private int propertyId;
    private String selectedRoomType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Room");
        }

        propertyId = getIntent().getIntExtra("PROPERTY_ID", -1);
        int roomId = getIntent().getIntExtra("ROOM_ID", -1); // Allow passing ROOM_ID for editing

        if (propertyId == -1 && roomId == -1) {
            finish();
            return;
        }

        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);

        EditText etNumber = findViewById(R.id.et_room_number);
        AutoCompleteTextView roomTypeDropdown = findViewById(R.id.room_type_dropdown);
        EditText etRent = findViewById(R.id.et_room_rent);
        EditText etSecurityDeposit = findViewById(R.id.et_room_security_deposit); // Add this field
        Button btnSave = findViewById(R.id.btn_save_room);

        TextInputLayout tilRent = findViewById(R.id.til_room_rent);
        TextInputLayout tilDeposit = findViewById(R.id.til_room_security_deposit);

        tilRent.setEndIconOnClickListener(v -> {
            CalculatorDialog dialog = new CalculatorDialog();
            dialog.setListener(result -> etRent.setText(result));
            dialog.show(getSupportFragmentManager(), "calculator");
        });

        tilDeposit.setEndIconOnClickListener(v -> {
            CalculatorDialog dialog = new CalculatorDialog();
            dialog.setListener(result -> etSecurityDeposit.setText(result));
            dialog.show(getSupportFragmentManager(), "calculator");
        });

        // Setup room type dropdown
        String[] roomTypes = {
            "Master Bedroom",
            "Non AC room(Front)",
            "Non AC room(shed)",
            "AC Room(R.H.S.)",
            "AC Room(L.H.S.)"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roomTypes);
        roomTypeDropdown.setAdapter(adapter);
        roomTypeDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedRoomType = roomTypes[position];
        });

        if (roomId != -1) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Room");
            roomViewModel.getRoomById(roomId).observe(this, room -> {
                if (room != null) {
                    etNumber.setText(room.roomNumber);
                    etRent.setText(String.valueOf(room.baseRent));
                    etSecurityDeposit.setText(String.valueOf(room.securityDeposit));
                    roomTypeDropdown.setText(room.roomType, false);
                    selectedRoomType = room.roomType;
                }
            });
        }

        btnSave.setOnClickListener(v -> {
            String number = etNumber.getText().toString().trim();
            String rentStr = etRent.getText().toString().trim();
            String depositStr = etSecurityDeposit.getText().toString().trim();

            if (number.isEmpty() || rentStr.isEmpty() || selectedRoomType.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double rent = Double.parseDouble(rentStr);
            double deposit = depositStr.isEmpty() ? 0 : Double.parseDouble(depositStr);

            Room room = new Room(propertyId, number, selectedRoomType, rent, deposit);
            if (roomId != -1) {
                room.id = roomId;
                if (propertyId != -1) room.propertyId = propertyId;
                roomViewModel.update(room);
            } else {
                roomViewModel.insert(room);
            }
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
