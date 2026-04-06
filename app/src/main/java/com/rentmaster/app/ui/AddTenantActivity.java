package com.rentmaster.app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.rentmaster.app.R;
import com.google.android.material.textfield.TextInputLayout;
import com.rentmaster.app.util.CalculatorDialog;
import com.rentmaster.app.data.entity.Tenant;
import com.rentmaster.app.ui.viewmodel.TenantViewModel;
import com.rentmaster.app.util.FormatUtils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTenantActivity extends AppCompatActivity {
    private TenantViewModel tenantViewModel;
    private int roomId;
    private String selectedRentFrequency = "";
    private long selectedStartDate = 0;
    private Calendar startDateCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tenant);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Tenant");
        }

        roomId = getIntent().getIntExtra("ROOM_ID", -1);
        int tenantId = getIntent().getIntExtra("TENANT_ID", -1);

        if (roomId == -1 && tenantId == -1) {
            finish();
            return;
        }

        tenantViewModel = new ViewModelProvider(this).get(TenantViewModel.class);

        EditText etName = findViewById(R.id.et_tenant_name);
        EditText etPhone = findViewById(R.id.et_tenant_phone);
        AutoCompleteTextView rentFrequencyDropdown = findViewById(R.id.rent_frequency_dropdown);
        EditText etStartDate = findViewById(R.id.et_start_date);
        EditText etDate = findViewById(R.id.et_start_date); // verify this line number
        EditText etDeposit = findViewById(R.id.et_security_deposit);
        // Removed etDueDay
        EditText etNotes = findViewById(R.id.et_tenant_notes);
        Button btnSave = findViewById(R.id.btn_save_tenant);

        TextInputLayout tilDeposit = findViewById(R.id.til_security_deposit);
        tilDeposit.setEndIconOnClickListener(v -> {
            CalculatorDialog dialog = new CalculatorDialog();
            dialog.setListener(result -> etDeposit.setText(result));
            dialog.show(getSupportFragmentManager(), "calculator");
        });

        // Setup rent frequency dropdown
        String[] frequencies = {"Weekly", "Fortnightly"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, frequencies);
        rentFrequencyDropdown.setAdapter(adapter);
        rentFrequencyDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedRentFrequency = frequencies[position];
        });

        // Setup date picker for start date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        etStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    startDateCalendar.set(year, month, dayOfMonth);
                    selectedStartDate = startDateCalendar.getTimeInMillis();
                    etStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
                },
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
            datePickerDialog.show();
        });

        if (tenantId != -1) {
             if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Tenant");
             // Fetch tenant to populate fields.
             // We need to fetch async.
             tenantViewModel.getTenantById(tenantId).observe(this, tenant -> {
                 if (tenant != null) {
                     etName.setText(tenant.name);
                     etPhone.setText(tenant.phone);
                     etNotes.setText(tenant.notes);
                     etDeposit.setText(String.valueOf(tenant.securityDeposit));
                     // Removed dueDay population
                     
                     selectedRentFrequency = tenant.rentFrequency;
                     rentFrequencyDropdown.setText(selectedRentFrequency, false);
                     
                     selectedStartDate = tenant.startDate;
                     startDateCalendar.setTimeInMillis(selectedStartDate);
                      etStartDate.setText(FormatUtils.formatDateWithMonth(startDateCalendar.getTimeInMillis()));
                     
                     // Ensure roomId is preserved if not passed
                     if (roomId == -1 && tenant.roomId != null) {
                         roomId = tenant.roomId;
                     }
                 }
             });
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String depositStr = etDeposit.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();
            // Removed dueDayStr reading

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Name and Phone are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!FormatUtils.isValidAustralianPhone(phone)) {
                Toast.makeText(this, FormatUtils.getPhoneValidationError(), Toast.LENGTH_LONG).show();
                return;
            }

            if (selectedRentFrequency.isEmpty()) {
                Toast.makeText(this, "Please select rent frequency", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedStartDate == 0) {
                Toast.makeText(this, "Please select start date", Toast.LENGTH_SHORT).show();
                return;
            }

            double deposit = depositStr.isEmpty() ? 0 : Double.parseDouble(depositStr);

            Tenant tenant = new Tenant(name, phone, notes, deposit, true, selectedRentFrequency, selectedStartDate);
            
            if (tenantId != -1) {
                tenant.id = tenantId;
                if (roomId != -1) tenant.roomId = roomId; // Preserve or update room
                else { 
                   // If editing and no room passed, keep existing? 
                   // We handled getting roomId from existing tenant above if roomId was -1.
                   tenant.roomId = roomId; 
                }
                tenantViewModel.update(tenant);
                finish();
            } else {
                tenant.roomId = roomId;
                // Fetch room to get baseRent for initial record
                com.rentmaster.app.ui.viewmodel.RoomViewModel roomViewModel = 
                    new ViewModelProvider(this).get(com.rentmaster.app.ui.viewmodel.RoomViewModel.class);
                
                roomViewModel.getRoomById(roomId).observe(this, room -> {
                    if (room != null) {
                        tenantViewModel.insertWithInitialRent(tenant, room.baseRent);
                        finish();
                    } else {
                        Toast.makeText(this, "Error: Room not found", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
