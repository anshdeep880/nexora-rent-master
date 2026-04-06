package com.rentmaster.app.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rentmaster.app.R;
import com.rentmaster.app.ui.adapter.ExpenseAdapter;
import com.rentmaster.app.ui.viewmodel.ExpenseViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ExpenseActivity extends AppCompatActivity {
    private ExpenseViewModel expenseViewModel;
    private int propertyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Property Expenses");
        }

        propertyId = getIntent().getIntExtra("PROPERTY_ID", -1);
        if (propertyId == -1) {
            finish();
            return;
        }

        RecyclerView rv = findViewById(R.id.rv_expenses);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ExpenseAdapter adapter = new ExpenseAdapter();
        rv.setAdapter(adapter);

        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        expenseViewModel.getExpensesForProperty(propertyId).observe(this, expenses -> {
            adapter.setExpenses(expenses);
        });

        FloatingActionButton fab = findViewById(R.id.fab_add_expense);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseActivity.this, AddExpenseActivity.class);
            intent.putExtra("PROPERTY_ID", propertyId);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
