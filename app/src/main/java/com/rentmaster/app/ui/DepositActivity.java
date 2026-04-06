package com.rentmaster.app.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rentmaster.app.R;
import com.rentmaster.app.data.AppDatabase;
import com.rentmaster.app.data.entity.Deposit;
import com.rentmaster.app.ui.adapter.DepositAdapter;
import com.rentmaster.app.ui.viewmodel.TenantViewModel;
import com.rentmaster.app.util.FormatUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;

public class DepositActivity extends AppCompatActivity {
    private int tenantId;
    private DepositAdapter adapter;
    private AppDatabase db;
    private TextView tvTotalDeposit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Deposits");
        }

        tenantId = getIntent().getIntExtra("TENANT_ID", -1);
        if (tenantId == -1) {
            finish();
            return;
        }

        db = AppDatabase.getDatabase(this);

        tvTotalDeposit = findViewById(R.id.tv_total_deposit);
        RecyclerView recyclerView = findViewById(R.id.rv_deposits);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new DepositAdapter();
        recyclerView.setAdapter(adapter);

        loadDeposits();

        findViewById(R.id.btn_add_deposit).setOnClickListener(v -> showAddDepositDialog());
    }

    private void loadDeposits() {
        // Run on background or use LiveData if possible. 
        // For simplicity using executor.
        com.rentmaster.app.data.repository.PropertyRepository.databaseWriteExecutor.execute(() -> {
            java.util.List<Deposit> deposits = db.depositDao().getDepositsForTenantSync(tenantId);
            double total = 0;
            for (Deposit d : deposits) {
                if (!d.refunded) total += d.amount;
            }
            double finalTotal = total;
            runOnUiThread(() -> {
                adapter.setDeposits(deposits);
                tvTotalDeposit.setText("Total Active Deposit: " + FormatUtils.formatCurrency(finalTotal));
            });
        });
    }

    private void showAddDepositDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_deposit, null);
        EditText etAmount = view.findViewById(R.id.et_deposit_amount);
        EditText etNotes = view.findViewById(R.id.et_deposit_notes);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Deposit")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String amountStr = etAmount.getText().toString();
                    String notes = etNotes.getText().toString();
                    if (!amountStr.isEmpty()) {
                        double amount = Double.parseDouble(amountStr);
                        Deposit deposit = new Deposit(tenantId, amount, System.currentTimeMillis(), notes, false);
                        com.rentmaster.app.data.repository.PropertyRepository.databaseWriteExecutor.execute(() -> {
                            db.depositDao().insert(deposit);
                            loadDeposits();
                        });
                    }
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
