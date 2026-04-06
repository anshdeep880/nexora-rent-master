package com.rentmaster.app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.rentmaster.app.data.AppDatabase;
import com.rentmaster.app.data.dao.ExpenseDao;
import com.rentmaster.app.data.entity.Expense;
import java.util.List;

public class ExpenseRepository {
    private ExpenseDao expenseDao;

    public ExpenseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        expenseDao = db.expenseDao();
    }

    public LiveData<List<Expense>> getExpensesForProperty(int propertyId) {
        return expenseDao.getExpensesForProperty(propertyId);
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return expenseDao.getAllExpenses();
    }

    public LiveData<Double> getTotalExpenses() {
        return expenseDao.getTotalExpenses();
    }

    public void insert(Expense expense) {
        PropertyRepository.databaseWriteExecutor.execute(() -> expenseDao.insert(expense));
    }

    public void update(Expense expense) {
        PropertyRepository.databaseWriteExecutor.execute(() -> expenseDao.update(expense));
    }

    public void delete(Expense expense) {
        PropertyRepository.databaseWriteExecutor.execute(() -> expenseDao.delete(expense));
    }
}
