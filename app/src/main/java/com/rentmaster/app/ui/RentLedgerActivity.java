package com.rentmaster.app.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rentmaster.app.R;
import com.rentmaster.app.ui.adapter.RentRecordAdapter;
import com.rentmaster.app.ui.viewmodel.RentViewModel;
import com.rentmaster.app.ui.viewmodel.RoomViewModel;
import com.rentmaster.app.ui.viewmodel.PropertyViewModel;
import com.rentmaster.app.data.entity.Property;
import com.rentmaster.app.data.entity.Room;
import java.util.ArrayList;
import java.util.List;

public class RentLedgerActivity extends AppCompatActivity {
    private RentViewModel rentViewModel;
    private RoomViewModel roomViewModel;
    private PropertyViewModel propertyViewModel;
    private RentRecordAdapter adapter;
    private int selectedPropertyId = -1;
    private int selectedRoomId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_ledger);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Rent Ledger");
        }

        RecyclerView rv = findViewById(R.id.rv_global_ledger);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RentRecordAdapter(new RentRecordAdapter.OnRecordClickListener() {
            @Override
            public void onRecordClick(com.rentmaster.app.data.entity.RentRecordWithDetails record) {
                Intent intent = new Intent(RentLedgerActivity.this, RecordPaymentActivity.class);
                intent.putExtra("RENT_RECORD_ID", record.rentRecord.id);
                startActivity(intent);
            }

            @Override
            public void onEditClick(com.rentmaster.app.data.entity.RentRecordWithDetails record) {
                showEditRentDialog(record.rentRecord);
            }

            @Override
            public void onDeleteClick(com.rentmaster.app.data.entity.RentRecordWithDetails record) {
                new androidx.appcompat.app.AlertDialog.Builder(RentLedgerActivity.this)
                        .setTitle("Delete Rent Record")
                        .setMessage("Are you sure you want to delete this rent record? This will also delete any associated payments and update reports.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            rentViewModel.delete(record.rentRecord);
                            android.widget.Toast.makeText(RentLedgerActivity.this, "Rent Record Deleted", android.widget.Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        rv.setAdapter(adapter);

        rentViewModel = new ViewModelProvider(this).get(RentViewModel.class);
        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        propertyViewModel = new ViewModelProvider(this).get(PropertyViewModel.class);

        android.widget.EditText etSearch = findViewById(R.id.et_search);
        android.widget.ImageButton btnFilter = findViewById(R.id.btn_filter);
        android.view.View emptyView = findViewById(com.rentmaster.app.R.id.tv_empty);
        // RecyclerView rv = findViewById(R.id.rv_global_ledger); // Already declared above

        btnFilter.setOnClickListener(v -> showFilterDialog());

        rentViewModel.getAllRentRecordsWithDetails().observe(this, records -> {
            adapter.setRecords(records);
            if (records == null || records.isEmpty()) {
                emptyView.setVisibility(android.view.View.VISIBLE);
                rv.setVisibility(android.view.View.GONE);
                etSearch.setVisibility(android.view.View.GONE);
            } else {
                emptyView.setVisibility(android.view.View.GONE);
                rv.setVisibility(android.view.View.VISIBLE);
                etSearch.setVisibility(android.view.View.VISIBLE);
            }
        });

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Auto-refresh data when coming back to ledger
        androidx.work.OneTimeWorkRequest refreshRequest = new androidx.work.OneTimeWorkRequest.Builder(
                com.rentmaster.app.worker.PeriodicRentWorker.class).build();
        androidx.work.WorkManager.getInstance(this).enqueue(refreshRequest);
    }

    private void showFilterDialog() {
        android.view.View view = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_filter_ledger, null);
        android.widget.Spinner spinnerProperty = view.findViewById(R.id.spinner_property);
        android.widget.Spinner spinnerRoom = view.findViewById(R.id.spinner_room);

        final List<Property> propertiesList = new ArrayList<>();
        final List<Room> roomsList = new ArrayList<>();

        // Load Properties
        propertyViewModel.getAllProperties().observe(this, properties -> {
            propertiesList.clear();
            propertiesList.addAll(properties);

            List<String> propertyNames = new ArrayList<>();
            propertyNames.add("All Properties");
            for (Property p : properties) propertyNames.add(p.name);

            android.widget.ArrayAdapter<String> pAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, propertyNames);
            pAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProperty.setAdapter(pAdapter);

            // Restore selection
            if (selectedPropertyId != -1) {
                for (int i = 0; i < properties.size(); i++) {
                    if (properties.get(i).id == selectedPropertyId) {
                        spinnerProperty.setSelection(i + 1);
                        break;
                    }
                }
            }
        });

        spinnerProperty.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position == 0) {
                    spinnerRoom.setEnabled(false);
                    android.widget.ArrayAdapter<String> emptyAdapter = new android.widget.ArrayAdapter<>(RentLedgerActivity.this, android.R.layout.simple_spinner_item, new String[]{"Select Property First"});
                    spinnerRoom.setAdapter(emptyAdapter);
                } else {
                    spinnerRoom.setEnabled(true);
                    Property selectedProp = propertiesList.get(position - 1);
                    roomViewModel.getRoomsForProperty(selectedProp.id).observe(RentLedgerActivity.this, rooms -> {
                        roomsList.clear();
                        roomsList.addAll(rooms);
                        List<String> roomNames = new ArrayList<>();
                        roomNames.add("All Rooms");
                        for (Room r : rooms) roomNames.add("Room " + r.roomNumber);

                        android.widget.ArrayAdapter<String> rAdapter = new android.widget.ArrayAdapter<>(RentLedgerActivity.this, android.R.layout.simple_spinner_item, roomNames);
                        rAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerRoom.setAdapter(rAdapter);

                        // Restore selection if property matches
                        if (selectedRoomId != -1) {
                            for (int i = 0; i < rooms.size(); i++) {
                                if (rooms.get(i).id == selectedRoomId) {
                                    spinnerRoom.setSelection(i + 1);
                                    break;
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Filter Ledger")
                .setView(view)
                .setPositiveButton("Apply", (dialog, which) -> {
                    int pPos = spinnerProperty.getSelectedItemPosition();
                    int rPos = spinnerRoom.getSelectedItemPosition();

                    selectedPropertyId = (pPos == 0) ? -1 : propertiesList.get(pPos - 1).id;
                    selectedRoomId = (rPos <= 0 || !spinnerRoom.isEnabled()) ? -1 : roomsList.get(rPos - 1).id;

                    adapter.setFilters(selectedPropertyId, selectedRoomId);
                })
                .setNeutralButton("Clear Filter", (dialog, which) -> {
                    selectedPropertyId = -1;
                    selectedRoomId = -1;
                    adapter.setFilters(-1, -1);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditRentDialog(com.rentmaster.app.data.entity.RentRecord record) {
        android.view.View view = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_add_rent_record, null);
        com.google.android.material.textfield.TextInputEditText etStartDate = view.findViewById(R.id.et_start_date);
        android.widget.EditText etAmount = view.findViewById(R.id.et_amount);
        android.widget.EditText etAmountPaid = view.findViewById(R.id.et_amount_paid);

        // Hide Rent Amount field as requested for Edits
        if (etAmount.getParent() instanceof android.view.View && ((android.view.View) etAmount.getParent())
                .getParent() instanceof com.google.android.material.textfield.TextInputLayout) {
            ((com.google.android.material.textfield.TextInputLayout) ((android.view.View) etAmount.getParent())
                    .getParent()).setVisibility(android.view.View.GONE);
        } else {
            etAmount.setVisibility(android.view.View.GONE);
        }

        etAmount.setText(String.valueOf(record.amountDue));
        etAmountPaid.setText(String.valueOf(record.amountPaid));
        etStartDate.setText(com.rentmaster.app.util.FormatUtils.formatDateWithMonth(record.periodStart));

        final java.util.Calendar selectedDate = java.util.Calendar.getInstance();
        selectedDate.setTimeInMillis(record.periodStart);

        etStartDate.setOnClickListener(v -> {
            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(this,
                    (picker, year, month, day) -> {
                        selectedDate.set(year, month, day);
                        etStartDate.setText(com.rentmaster.app.util.FormatUtils
                                .formatDateWithMonth(selectedDate.getTimeInMillis()));
                    }, selectedDate.get(java.util.Calendar.YEAR), selectedDate.get(java.util.Calendar.MONTH),
                    selectedDate.get(java.util.Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Rent Record")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    String amountStr = etAmount.getText().toString().trim();
                    String amountPaidStr = etAmountPaid.getText().toString().trim();

                    if (etStartDate.getText().toString().isEmpty() || amountStr.isEmpty()) {
                        android.widget.Toast
                                .makeText(this, "Filling all fields is required", android.widget.Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    double amount = Double.parseDouble(amountStr);
                    double amountPaidVal = amountPaidStr.isEmpty() ? 0 : Double.parseDouble(amountPaidStr);
                    long periodStart = selectedDate.getTimeInMillis();

                    // Maintain the same duration as before
                    long duration = record.periodEnd - record.periodStart;
                    record.amountDue = amount;
                    record.amountPaid = amountPaidVal;
                    record.periodStart = periodStart;
                    record.periodEnd = periodStart + duration;
                    record.dueDate = periodStart; // Assuming due date matches period start

                    java.util.Calendar pCal = java.util.Calendar.getInstance();
                    pCal.setTimeInMillis(periodStart);
                    record.month = pCal.get(java.util.Calendar.MONTH);
                    record.year = pCal.get(java.util.Calendar.YEAR);

                    // Recalculate status
                    if (record.amountPaid >= record.amountDue) {
                        record.status = "Paid";
                    } else if (record.amountPaid > 0) {
                        record.status = "Partial";
                    } else {
                        record.status = "Unpaid";
                    }

                    rentViewModel.update(record);
                    android.widget.Toast.makeText(this, "Rent Record Updated", android.widget.Toast.LENGTH_SHORT)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
