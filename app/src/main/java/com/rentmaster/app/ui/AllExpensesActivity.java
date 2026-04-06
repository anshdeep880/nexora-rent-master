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
import com.rentmaster.app.ui.viewmodel.PropertyViewModel;

public class AllExpensesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_expenses);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("All Expenses");
        }

        RecyclerView rv = findViewById(R.id.rv_all_expenses);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ExpenseAdapter adapter = new ExpenseAdapter();
        rv.setAdapter(adapter);

        ExpenseViewModel expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        // Need a method to get all expenses in ExpensesViewModel/Repository if not exists
        expenseViewModel.getAllExpenses().observe(this, adapter::setExpenses);

        findViewById(R.id.fab_add_expense_global).setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Select Property");
            
            // Fetch properties to show in list
            new ViewModelProvider(this).get(PropertyViewModel.class)
                .getAllProperties().observe(this, properties -> {
                    if (properties == null || properties.isEmpty()) {
                        android.widget.Toast.makeText(this, "Add a property first", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] names = new String[properties.size()];
                    for (int i = 0; i < properties.size(); i++) names[i] = properties.get(i).name;
                    
                    builder.setItems(names, (dialog, which) -> {
                        Intent intent = new Intent(this, AddExpenseActivity.class);
                        intent.putExtra("PROPERTY_ID", properties.get(which).id);
                        startActivity(intent);
                    });
                    builder.show();
                });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
