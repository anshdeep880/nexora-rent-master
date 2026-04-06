package com.rentmaster.app.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import com.rentmaster.app.R;
import com.rentmaster.app.data.entity.Property;
import com.rentmaster.app.ui.adapter.PropertyAdapter;
import com.rentmaster.app.ui.viewmodel.PropertyViewModel;
import com.rentmaster.app.ui.viewmodel.RentViewModel;
import com.rentmaster.app.ui.viewmodel.ExpenseViewModel;
import com.rentmaster.app.util.FormatUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements PropertyAdapter.OnPropertyInteractionListener {
    private PropertyViewModel propertyViewModel;
    private RentViewModel rentViewModel;
    private ExpenseViewModel expenseViewModel;
    private PropertyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.rv_properties);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PropertyAdapter(this);
        recyclerView.setAdapter(adapter);

        propertyViewModel = new ViewModelProvider(this).get(PropertyViewModel.class);
        rentViewModel = new ViewModelProvider(this).get(RentViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        propertyViewModel.getPropertiesWithRoomCount().observe(this, properties -> {
            adapter.setProperties(properties);
        });

        setupDashboard();
        setupMonthlyRentWorker();

        FloatingActionButton fab = findViewById(R.id.fab_add_property);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddPropertyActivity.class);
            startActivity(intent);
        });

        setupBottomNavigation();

        checkSmsPermission(); // Restored as per user request

        findViewById(R.id.btn_refresh_data).setOnClickListener(v -> {
            androidx.work.OneTimeWorkRequest refreshRequest = new androidx.work.OneTimeWorkRequest.Builder(
                    com.rentmaster.app.worker.PeriodicRentWorker.class).build();
            androidx.work.WorkManager.getInstance(this).enqueue(refreshRequest);
            android.widget.Toast.makeText(this, "Refreshing rent records...", android.widget.Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_settings).setOnClickListener(v -> showPinUpdateDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Auto-refresh data when coming back to dashboard
        androidx.work.OneTimeWorkRequest refreshRequest = new androidx.work.OneTimeWorkRequest.Builder(
                com.rentmaster.app.worker.PeriodicRentWorker.class).build();
        androidx.work.WorkManager.getInstance(this).enqueue(refreshRequest);
    }

    private void showPinUpdateDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final android.widget.EditText etOldPin = new android.widget.EditText(this);
        etOldPin.setHint("Current PIN");
        etOldPin.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        layout.addView(etOldPin);

        final android.widget.EditText etNewPin = new android.widget.EditText(this);
        etNewPin.setHint("New PIN");
        etNewPin.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        layout.addView(etNewPin);

        final android.widget.EditText etConfirmPin = new android.widget.EditText(this);
        etConfirmPin.setHint("Confirm New PIN");
        etConfirmPin.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        layout.addView(etConfirmPin);

        new AlertDialog.Builder(this)
                .setTitle("Update Login PIN")
                .setView(layout)
                .setPositiveButton("Update", (dialog, which) -> {
                    String oldPin = etOldPin.getText().toString();
                    String newPin = etNewPin.getText().toString();
                    String confirmPin = etConfirmPin.getText().toString();

                    android.content.SharedPreferences prefs = getSharedPreferences("RentMasterPrefs", MODE_PRIVATE);
                    String savedPin = prefs.getString("APP_PIN", "1234");

                    if (!oldPin.equals(savedPin)) {
                        android.widget.Toast.makeText(this, "Incorrect current PIN", android.widget.Toast.LENGTH_SHORT)
                                .show();
                    } else if (newPin.length() < 4) {
                        android.widget.Toast
                                .makeText(this, "PIN must be at least 4 digits", android.widget.Toast.LENGTH_SHORT)
                                .show();
                    } else if (!newPin.equals(confirmPin)) {
                        android.widget.Toast.makeText(this, "Pins do not match", android.widget.Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        prefs.edit().putString("APP_PIN", newPin).apply();
                        android.widget.Toast
                                .makeText(this, "PIN updated successfully", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Reset App", (dialog, which) -> {
                    showResetConfirmationDialog();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset App - Warning!")
                .setMessage(
                        "⚠️ THIS WILL DELETE ALL DATA\n\nThis action will permanently remove:\n• All Properties\n• All Rooms\n• All Tenants\n• All Rent Records\n• All Payments\n• All Expenses\n\nThis cannot be undone!\n\nAre you sure you want to continue?")
                .setPositiveButton("Yes, Reset Everything", (dialog, which) -> {
                    resetApp();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void resetApp() {
        // Show progress
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Resetting app...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Run reset in background thread
        new Thread(() -> {
            // Reset database
            com.rentmaster.app.data.AppDatabase.resetDatabase(this);

            // Clear shared preferences
            android.content.SharedPreferences prefs = getSharedPreferences("RentMasterPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            prefs.edit().putString("APP_PIN", "1234").apply(); // Reset to default PIN

            // Return to main thread
            runOnUiThread(() -> {
                progressDialog.dismiss();
                android.widget.Toast
                        .makeText(this, "App reset complete. Restarting...", android.widget.Toast.LENGTH_LONG).show();

                // Restart app
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                System.exit(0);
            });
        }).start();
    }

    private void setupBottomNavigation() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(
                R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_properties) {
                return true;
            } else if (itemId == R.id.nav_tenants) {
                startActivity(new Intent(this, TenantListActivity.class));
                return true;
            } else if (itemId == R.id.nav_landlord) {
                startActivity(new Intent(this, LandlordDetailActivity.class));
                return true;
            } else if (itemId == R.id.nav_ledger) {
                startActivity(new Intent(this, RentLedgerActivity.class));
                return true;
            } else if (itemId == R.id.nav_reports) {
                startActivity(new Intent(this, ReportActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupMonthlyRentWorker() {
        androidx.work.PeriodicWorkRequest rentRequest = new androidx.work.PeriodicWorkRequest.Builder(
                com.rentmaster.app.worker.PeriodicRentWorker.class, 24, java.util.concurrent.TimeUnit.HOURS)
                .build();
        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "PeriodicRentGeneration",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                rentRequest);
    }

    private void setupDashboard() {
        TextView tvCollected = findViewById(R.id.tv_collected);
        TextView tvExpenses = findViewById(R.id.tv_expenses);

        rentViewModel.getTotalPaid().observe(this, paid -> {
            tvCollected.setText(FormatUtils.formatCurrency(paid != null ? paid : 0.0));
        });

        expenseViewModel.getTotalExpenses().observe(this, expenses -> {
            tvExpenses.setText(FormatUtils.formatCurrency(expenses != null ? expenses : 0.0));
        });

        findViewById(R.id.layout_collected)
                .setOnClickListener(v -> startActivity(new Intent(this, RentLedgerActivity.class)));
        findViewById(R.id.layout_expenses)
                .setOnClickListener(v -> startActivity(new Intent(this, AllExpensesActivity.class)));
    }

    @Override
    public void onPropertyClick(Property property) {
        Intent intent = new Intent(MainActivity.this, PropertyDetailActivity.class);
        intent.putExtra("PROPERTY_ID", property.id);
        startActivity(intent);
    }

    @Override
    public void onEditClick(Property property) {
        Intent intent = new Intent(this, AddPropertyActivity.class);
        intent.putExtra("PROPERTY_ID", property.id);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Property property) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Property")
                .setMessage(
                        "Are you sure you want to delete this property? All associated rooms and tenants will be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    propertyViewModel.delete(property);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkSmsPermission() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.SEND_SMS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this,
                    new String[] { android.Manifest.permission.SEND_SMS }, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions,
            @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                android.widget.Toast.makeText(this, "SMS Permission Granted", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                android.widget.Toast.makeText(this, "SMS Permission is required for notifications",
                        android.widget.Toast.LENGTH_LONG).show();
            }
        }
    }
}
