package com.rentmaster.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.rentmaster.app.R;
import com.google.android.material.textfield.TextInputLayout;
import com.rentmaster.app.util.CalculatorDialog;
import com.rentmaster.app.data.entity.Payment;
import com.rentmaster.app.data.entity.RentRecord;
import com.rentmaster.app.data.entity.Tenant;
import com.rentmaster.app.ui.viewmodel.RentViewModel;
import com.rentmaster.app.ui.viewmodel.TenantViewModel;
import com.rentmaster.app.util.FormatUtils;
import com.rentmaster.app.util.PdfReportGenerator;
import java.util.Date;

public class RecordPaymentActivity extends AppCompatActivity {
    private RentViewModel rentViewModel;
    private TenantViewModel tenantViewModel;
    private int rentRecordId;
    private RentRecord currentRecord;
    private Tenant currentTenant;
    private Payment lastPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_payment);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Record Payment");
        }

        rentRecordId = getIntent().getIntExtra("RENT_RECORD_ID", -1);
        if (rentRecordId == -1) {
            finish();
            return;
        }

        rentViewModel = new ViewModelProvider(this).get(RentViewModel.class);
        tenantViewModel = new ViewModelProvider(this).get(TenantViewModel.class);

        EditText etAmount = findViewById(R.id.et_payment_amount);
        EditText etMode = findViewById(R.id.et_payment_mode);
        EditText etRef = findViewById(R.id.et_payment_reference);
        Button btnSave = findViewById(R.id.btn_record_payment);
        Button btnReceipt = findViewById(R.id.btn_generate_receipt);

        TextInputLayout tilAmount = findViewById(R.id.til_payment_amount);
        tilAmount.setEndIconOnClickListener(v -> {
            CalculatorDialog dialog = new CalculatorDialog();
            dialog.setListener(result -> etAmount.setText(result));
            dialog.show(getSupportFragmentManager(), "calculator");
        });

        TextView tvSubtitle = findViewById(R.id.tv_payment_subtitle);
        rentViewModel.getRentRecordById(rentRecordId).observe(this, record -> {
            if (record != null) {
                currentRecord = record;
                String monthName = new java.text.DateFormatSymbols().getMonths()[record.month];
                tvSubtitle.setText(String.format(java.util.Locale.getDefault(),
                        "Paying for: %s %d | Due: %s", monthName, record.year,
                        FormatUtils.formatCurrency(record.amountDue)));

                tenantViewModel.getTenantById(record.tenantId).observe(this, tenant -> {
                    currentTenant = tenant;
                });
            }
        });

        btnSave.setOnClickListener(v -> {
            if (currentRecord == null)
                return;

            String amountStr = etAmount.getText().toString().trim();
            String mode = etMode.getText().toString().trim();
            String ref = etRef.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Amount is required", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            lastPayment = new Payment(rentRecordId, currentRecord.tenantId, amount, new Date().getTime(), mode, ref);

            rentViewModel.addPayment(lastPayment, currentRecord);
            Toast.makeText(this, "Payment Recorded Successfully", Toast.LENGTH_SHORT).show();

            btnSave.setEnabled(false);
            btnReceipt.setVisibility(View.VISIBLE);
        });

        btnReceipt.setOnClickListener(v -> {
            if (currentTenant != null && lastPayment != null) {
                java.io.File file = PdfReportGenerator.generateReceipt(this, currentTenant, lastPayment);
                if (file != null) {
                    try {
                        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                                this,
                                getApplicationContext().getPackageName() + ".fileprovider",
                                file);

                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/pdf");
                        intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "No PDF Viewer found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_record_payment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Rent Record")
                    .setMessage("Are you sure you want to delete this rent record?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (currentRecord != null) {
                            rentViewModel.delete(currentRecord);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
