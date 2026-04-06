package com.rentmaster.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.rentmaster.app.data.entity.Payment;
import java.util.List;

@Dao
public interface PaymentDao {
    @Insert
    void insert(Payment payment);

    @Query("SELECT * FROM payments WHERE rentRecordId = :rentRecordId")
    LiveData<List<Payment>> getPaymentsForRecord(int rentRecordId);

    @Query("SELECT SUM(amount) FROM payments WHERE tenantId = :tenantId")
    double getTotalPaymentsForTenantSync(int tenantId);
}
