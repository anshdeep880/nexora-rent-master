package com.rentmaster.app.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rentmaster.app.R;
import com.rentmaster.app.data.entity.Tenant;
import com.rentmaster.app.ui.adapter.TenantAdapter;
import com.rentmaster.app.ui.viewmodel.TenantViewModel;

public class TenantListActivity extends AppCompatActivity implements TenantAdapter.OnTenantInteractionListener {
    private TenantViewModel tenantViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tenants");
        }

        RecyclerView recyclerView = findViewById(R.id.rv_all_tenants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        TenantAdapter adapter = new TenantAdapter(this);
        recyclerView.setAdapter(adapter);

        tenantViewModel = new ViewModelProvider(this).get(TenantViewModel.class);

        android.view.View emptyView = findViewById(R.id.tv_empty);
        android.widget.EditText etSearch = findViewById(R.id.et_search);

        tenantViewModel.getTenantsByCategory(true).observe(this, tenants -> {
            adapter.setTenants(tenants);
            if (tenants == null || tenants.isEmpty()) {
                emptyView.setVisibility(android.view.View.VISIBLE);
                recyclerView.setVisibility(android.view.View.GONE);
                etSearch.setVisibility(android.view.View.GONE);
            } else {
                emptyView.setVisibility(android.view.View.GONE);
                recyclerView.setVisibility(android.view.View.VISIBLE);
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
        // Auto-refresh data when coming back to list
        androidx.work.OneTimeWorkRequest refreshRequest = new androidx.work.OneTimeWorkRequest.Builder(
                com.rentmaster.app.worker.PeriodicRentWorker.class).build();
        androidx.work.WorkManager.getInstance(this).enqueue(refreshRequest);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onTenantClick(Tenant tenant) {
        Intent intent = new Intent(TenantListActivity.this, RoomDetailActivity.class);
        intent.putExtra("ROOM_ID", tenant.roomId);
        startActivity(intent);
    }

    @Override
    public void onEditClick(Tenant tenant) {
        Intent intent = new Intent(this, AddTenantActivity.class);
        intent.putExtra("TENANT_ID", tenant.id);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Tenant tenant) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Tenant")
                .setMessage("Are you sure you want to delete this tenant?")
                .setPositiveButton("Delete", (dialog, which) -> tenantViewModel.delete(tenant))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
