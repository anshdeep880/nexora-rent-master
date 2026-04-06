package com.rentmaster.app.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.rentmaster.app.data.entity.Expense;
import com.rentmaster.app.data.repository.ExpenseRepository;
import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {
    private ExpenseRepository repository;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return repository.getAllExpenses();
    }

    public LiveData<Double> getTotalExpenses() {
        return repository.getTotalExpenses();
    }

    public LiveData<List<Expense>> getExpensesForProperty(int propertyId) {
        return repository.getExpensesForProperty(propertyId);
    }

    public void insert(Expense expense) {
        repository.insert(expense);
    }
}
