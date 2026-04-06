package com.rentmaster.app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.rentmaster.app.data.AppDatabase;
import com.rentmaster.app.data.dao.PaymentDao;
import com.rentmaster.app.data.entity.Payment;
import java.util.List;

public class PaymentRepository {
    private PaymentDao paymentDao;

    public PaymentRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        paymentDao = db.paymentDao();
    }

    public LiveData<List<Payment>> getPaymentsForRecord(int rentRecordId) {
        return paymentDao.getPaymentsForRecord(rentRecordId);
    }

    public void insert(Payment payment) {
        PropertyRepository.databaseWriteExecutor.execute(() -> paymentDao.insert(payment));
    }
}
