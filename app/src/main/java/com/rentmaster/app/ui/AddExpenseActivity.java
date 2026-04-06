package com.rentmaster.app.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.rentmaster.app.R;
import com.google.android.material.textfield.TextInputLayout;
import com.rentmaster.app.util.CalculatorDialog;
import com.rentmaster.app.data.entity.Expense;
import com.rentmaster.app.ui.viewmodel.ExpenseViewModel;
import java.util.Date;

public class AddExpenseActivity extends AppCompatActivity {
    private ExpenseViewModel expenseViewModel;
    private int propertyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Expense");
        }

        propertyId = getIntent().getIntExtra("PROPERTY_ID", -1);
        if (propertyId == -1) {
            finish();
            return;
        }

        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        EditText etDesc = findViewById(R.id.et_expense_desc);
        EditText etAmount = findViewById(R.id.et_expense_amount);
        EditText etCategory = findViewById(R.id.et_expense_category);
        Button btnSave = findViewById(R.id.btn_save_expense);

        TextInputLayout tilAmount = findViewById(R.id.til_expense_amount);
        tilAmount.setEndIconOnClickListener(v -> {
            CalculatorDialog dialog = new CalculatorDialog();
            dialog.setListener(result -> etAmount.setText(result));
            dialog.show(getSupportFragmentManager(), "calculator");
        });

        btnSave.setOnClickListener(v -> {
            String desc = etDesc.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String category = etCategory.getText().toString().trim();

            if (desc.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Description and Amount are required", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            Expense expense = new Expense(propertyId, desc, amount, new Date().getTime(), category);
            expenseViewModel.insert(expense);
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
