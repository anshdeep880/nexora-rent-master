package com.rentmaster.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rentmaster.app.R;
import com.rentmaster.app.ui.adapter.RentRecordAdapter;
import com.rentmaster.app.ui.viewmodel.TenantViewModel;
import com.rentmaster.app.ui.viewmodel.RentViewModel;
import com.rentmaster.app.util.FormatUtils;
import com.google.android.material.button.MaterialButton;

public class RoomDetailActivity extends AppCompatActivity {
    private TenantViewModel tenantViewModel;
    private RentViewModel rentViewModel;
    private int roomId;
    private com.rentmaster.app.data.entity.Room currentRoom;
    private int currentTenantId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Room Details");
        }

        roomId = getIntent().getIntExtra("ROOM_ID", -1);
        if (roomId == -1) {
            finish();
            return;
        }

        tenantViewModel = new ViewModelProvider(this).get(TenantViewModel.class);
        rentViewModel = new ViewModelProvider(this).get(RentViewModel.class);
        com.rentmaster.app.ui.viewmodel.RoomViewModel roomViewModel = new ViewModelProvider(this)
                .get(com.rentmaster.app.ui.viewmodel.RoomViewModel.class);

        TextView tvRent = findViewById(R.id.tv_room_rent);
        TextView tvRoomType = findViewById(R.id.tv_room_type);
        roomViewModel.getRoomById(roomId).observe(this, room -> {
            if (room != null) {
                currentRoom = room;
                tvRent.setText("Rent: " + FormatUtils.formatCurrency(room.baseRent));
                tvRoomType.setText(room.roomType != null ? room.roomType : "Not specified");
            }
        });

        TextView tvStatus = findViewById(R.id.tv_tenant_status);
        TextView tvInfo = findViewById(R.id.tv_tenant_info);
        MaterialButton btnAssign = findViewById(R.id.btn_assign_tenant);
        MaterialButton btnVacate = findViewById(R.id.btn_vacate_tenant);

        tenantViewModel.getTenantForRoom(roomId).observe(this, tenant -> {
            if (tenant != null) {
                tvStatus.setText("Tenant: " + tenant.name);
                tvInfo.setText("Phone: " + tenant.phone);
                tvInfo.setVisibility(View.VISIBLE);
                // Also enable Deposit menu capability
                currentTenantId = tenant.id;
                btnAssign.setVisibility(View.GONE);
                btnVacate.setVisibility(View.VISIBLE);

                invalidateOptionsMenu(); // Force menu redraw to show deposit option

                btnVacate.setOnClickListener(v -> {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Vacate Tenant")
                            .setMessage("Are you sure you want to vacate " + tenant.name + "?")
                            .setPositiveButton("Vacate", (dialog, which) -> {
                                tenant.isActive = false;
                                tenantViewModel.update(tenant);
                                Toast.makeText(this, "Tenant vacated", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                loadRentHistory(tenant.id);
            } else {
                tvStatus.setText("No Tenant Assigned");
                tvInfo.setVisibility(View.GONE);
                btnAssign.setVisibility(View.VISIBLE);
                btnVacate.setVisibility(View.GONE);
            }
        });

        btnAssign.setOnClickListener(v -> {
            Intent intent = new Intent(RoomDetailActivity.this, AddTenantActivity.class);
            intent.putExtra("ROOM_ID", roomId);
            startActivity(intent);
        });

        MaterialButton btnLedger = findViewById(R.id.btn_room_ledger);
        btnLedger.setOnClickListener(v -> {
            Intent intent = new Intent(RoomDetailActivity.this, RoomLedgerActivity.class);
            intent.putExtra("ROOM_ID", roomId);
            startActivity(intent);
        });
    }

    private void loadRentHistory(int tenantId) {
        RecyclerView rv = findViewById(R.id.rv_rent_history);
        rv.setLayoutManager(new LinearLayoutManager(this));
        RentRecordAdapter adapter = new RentRecordAdapter(new RentRecordAdapter.OnRecordClickListener() {
            @Override
            public void onRecordClick(com.rentmaster.app.data.entity.RentRecordWithDetails record) {
                Intent intent = new Intent(RoomDetailActivity.this, RecordPaymentActivity.class);
                intent.putExtra("RENT_RECORD_ID", record.rentRecord.id);
                startActivity(intent);
            }

            @Override
            public void onEditClick(com.rentmaster.app.data.entity.RentRecordWithDetails record) {
                // Not reachable if setShowEditButton(false) is called
            }

            @Override
            public void onDeleteClick(com.rentmaster.app.data.entity.RentRecordWithDetails record) {
                new androidx.appcompat.app.AlertDialog.Builder(RoomDetailActivity.this)
                        .setTitle("Delete Rent Record")
                        .setMessage("Are you sure you want to delete this rent record? This will also delete any associated payments and update reports.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            rentViewModel.delete(record.rentRecord);
                            android.widget.Toast.makeText(RoomDetailActivity.this, "Rent Record Deleted", android.widget.Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        adapter.setShowEditButton(false);
        rv.setAdapter(adapter);

        rentViewModel.getRentHistoryForTenantWithDetails(tenantId).observe(this, records -> {
            adapter.setRecords(records);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_room_detail, menu);
        android.view.MenuItem deleteTenant = menu.findItem(R.id.action_delete_tenant);
        if (deleteTenant != null) {
            deleteTenant.setVisible(currentTenantId != -1);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_edit) {
            Intent intent = new Intent(this, AddRoomActivity.class);
            intent.putExtra("ROOM_ID", roomId);
            // We should also pass PROPERTY_ID if known, but RoomDetailActivity might not
            // know it easily unless we query room.
            // We have room object in observe. We should store it.
            // Let's rely on RoomDetailActivity having loaded the room.
            // I'll add a member variable 'currentRoom' and use it.
            if (currentRoom != null) {
                intent.putExtra("PROPERTY_ID", currentRoom.propertyId);
            }
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_deposit) {
            if (currentTenantId != -1) {
                Intent intent = new Intent(this, DepositActivity.class);
                intent.putExtra("TENANT_ID", currentTenantId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No Tenant Assigned", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (item.getItemId() == R.id.action_delete_room) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Room")
                    .setMessage("Are you sure you want to delete this room?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (currentRoom != null) {
                            com.rentmaster.app.ui.viewmodel.RoomViewModel roomVM = new androidx.lifecycle.ViewModelProvider(
                                    this).get(com.rentmaster.app.ui.viewmodel.RoomViewModel.class);
                            roomVM.delete(currentRoom);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        } else if (item.getItemId() == R.id.action_delete_tenant) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Tenant")
                    .setMessage("Are you sure you want to permanently delete this tenant?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (currentTenantId != -1) {
                            // We need the tenant object to delete. We can construct a dummy one or use
                            // observed one.
                            // But we don't have the object handy in this scope easily without storing it.
                            // Let's fetch it or use a dummy with ID.
                            com.rentmaster.app.data.entity.Tenant tenant = new com.rentmaster.app.data.entity.Tenant(
                                    null, null, null, 0, false, null, 0);
                            tenant.id = currentTenantId;
                            tenantViewModel.delete(tenant);
                            // Don't finish activity, just refresh (observation will update UI to "No
                            // Tenant")
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
