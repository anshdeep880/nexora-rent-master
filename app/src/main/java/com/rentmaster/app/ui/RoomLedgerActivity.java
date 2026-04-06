package com.rentmaster.app.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.rentmaster.app.R;
import com.rentmaster.app.data.entity.RentRecord;
import com.rentmaster.app.data.entity.Room;
import com.rentmaster.app.data.entity.Tenant;
import com.rentmaster.app.ui.adapter.RentRecordAdapter;
import com.rentmaster.app.ui.viewmodel.RentViewModel;
import com.rentmaster.app.ui.viewmodel.RoomViewModel;
import com.rentmaster.app.ui.viewmodel.TenantViewModel;
import com.rentmaster.app.util.FormatUtils;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RoomLedgerActivity extends AppCompatActivity {
    private RentViewModel rentViewModel;
    private RoomViewModel roomViewModel;
    private TenantViewModel tenantViewModel;
    private RentRecordAdapter adapter;
    private int roomId;
    private Room currentRoom;
    private Tenant currentTenant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_ledger);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Room Ledger");
        }

        roomId = getIntent().getIntExtra("ROOM_ID", -1);
        if (roomId == -1) {
            finish();
            return;
        }

        rentViewModel = new ViewModelProvider(this).get(RentViewModel.class);
        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        tenantViewModel = new ViewModelProvider(this).get(TenantViewModel.class);

        TextView tvRoomName = findViewById(R.id.tv_room_name);
        TextView tvRoomSummary = findViewById(R.id.tv_room_summary);
        RecyclerView recyclerView = findViewById(R.id.rv_room_ledger);
        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fab_add_manual_rent);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RentRecordAdapter(new RentRecordAdapter.OnRecordClickListener() {
            @Override
            public void onRecordClick(com.rentmaster.app.data.entity.RentRecordWithDetails record) {
                Intent intent = new Intent(RoomLedgerActivity.this, RecordPaymentActivity.class);
                intent.putExtra("RENT_RECORD_ID", record.rentRecord.id);
                startActivity(intent);
            }

            @Override
            public void onEditClick(com.rentmaster.app.data.entity.RentRecordWithDetails record) {
                showEditRentDialog(record.rentRecord);
            }

            @Override
            public void onDeleteClick(com.rentmaster.app.data.entity.RentRecordWithDetails record) {
                new androidx.appcompat.app.AlertDialog.Builder(RoomLedgerActivity.this)
                        .setTitle("Delete Rent Record")
                        .setMessage("Are you sure you want to delete this rent record? This will also delete any associated payments and update reports.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            rentViewModel.delete(record.rentRecord);
                            android.widget.Toast.makeText(RoomLedgerActivity.this, "Rent Record Deleted", android.widget.Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);

        roomViewModel.getRoomById(roomId).observe(this, room -> {
            if (room != null) {
                currentRoom = room;
                tvRoomName.setText("Room " + room.roomNumber);
            }
        });

        tenantViewModel.getTenantForRoom(roomId).observe(this, tenant -> {
            currentTenant = tenant;
            if (tenant != null) {
                tvRoomSummary.setText("Current Tenant: " + tenant.name);
            } else {
                tvRoomSummary.setText("No active tenant");
            }
        });

        EditText etSearch = findViewById(R.id.et_search);
        View emptyView = findViewById(com.rentmaster.app.R.id.tv_empty);
        // Using existing recyclerView variable

        rentViewModel.getRentHistoryForRoomWithDetails(roomId).observe(this, records -> {
            adapter.setRecords(records);
            if (records == null || records.isEmpty()) {
                etSearch.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                etSearch.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
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

        fabAdd.setOnClickListener(v -> showAddRentDialog());
    }

    private void showAddRentDialog() {
        if (currentRoom == null)
            return;
        if (currentTenant == null) {
            Toast.makeText(this, "Assign a tenant first to create rent records", Toast.LENGTH_SHORT).show();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_rent_record, null);
        TextInputEditText etStartDate = view.findViewById(R.id.et_start_date);
        EditText etAmount = view.findViewById(R.id.et_amount);
        EditText etAmountPaid = view.findViewById(R.id.et_amount_paid);

        double baseRent = currentRoom.baseRent;
        double initialAmount = baseRent;
        if ("Fortnightly".equalsIgnoreCase(currentTenant.rentFrequency)) {
            initialAmount = baseRent * 2;
        }
        etAmount.setText(String.valueOf(initialAmount));
        etAmountPaid.setText("0");

        final Calendar selectedDate = Calendar.getInstance();

        etStartDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (picker, year, month, day) -> {
                selectedDate.set(year, month, day);
                etStartDate.setText(FormatUtils.formatDateWithMonth(selectedDate.getTimeInMillis()));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Add Rent Record")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String amountStr = etAmount.getText().toString().trim();
                    String amountPaidStr = etAmountPaid.getText().toString().trim();

                    if (etStartDate.getText().toString().isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(this, "Filling all fields is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = Double.parseDouble(amountStr);
                    double amountPaid = amountPaidStr.isEmpty() ? 0 : Double.parseDouble(amountPaidStr);
                    long periodStart = selectedDate.getTimeInMillis();
                    int frequencyDays = "Weekly".equals(currentTenant.rentFrequency) ? 7 : 14;
                    Calendar endCal = (Calendar) selectedDate.clone();
                    endCal.add(Calendar.DAY_OF_YEAR, frequencyDays - 1);
                    long periodEnd = endCal.getTimeInMillis();

                    Calendar pCal = Calendar.getInstance();
                    pCal.setTimeInMillis(periodStart);
                    int m = pCal.get(Calendar.MONTH);
                    int y = pCal.get(Calendar.YEAR);

                    RentRecord record = new RentRecord(currentTenant.id, roomId, m, y, amount, periodStart, periodStart,
                            periodEnd);
                    record.amountPaid = amountPaid;
                    // Recalculate status for manually added record
                    if (record.amountPaid >= record.amountDue) {
                        record.status = "Paid";
                    } else if (record.amountPaid > 0) {
                        record.status = "Partial";
                    } else {
                        record.status = "Unpaid";
                    }

                    rentViewModel.insert(record);
                    Toast.makeText(this, "Rent Record Added", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditRentDialog(RentRecord record) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_rent_record, null);
        TextInputEditText etStartDate = view.findViewById(R.id.et_start_date);
        EditText etAmount = view.findViewById(R.id.et_amount);
        EditText etAmountPaid = view.findViewById(R.id.et_amount_paid);

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
        etStartDate.setText(FormatUtils.formatDateWithMonth(record.periodStart));

        final Calendar selectedDate = Calendar.getInstance();
        selectedDate.setTimeInMillis(record.periodStart);

        etStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (picker, year, month, day) -> {
                selectedDate.set(year, month, day);
                etStartDate.setText(FormatUtils.formatDateWithMonth(selectedDate.getTimeInMillis()));
            }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Edit Rent Record")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    String amountStr = etAmount.getText().toString().trim();
                    String amountPaidStr = etAmountPaid.getText().toString().trim();

                    if (etStartDate.getText().toString().isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(this, "Filling all fields is required", Toast.LENGTH_SHORT).show();
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

                    Calendar pCal = Calendar.getInstance();
                    pCal.setTimeInMillis(periodStart);
                    record.month = pCal.get(Calendar.MONTH);
                    record.year = pCal.get(Calendar.YEAR);

                    // Recalculate status
                    if (record.amountPaid >= record.amountDue) {
                        record.status = "Paid";
                    } else if (record.amountPaid > 0) {
                        record.status = "Partial";
                    } else {
                        record.status = "Unpaid";
                    }

                    rentViewModel.update(record);
                    Toast.makeText(this, "Rent Record Updated", Toast.LENGTH_SHORT).show();
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
