package com.rentmaster.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rentmaster.app.data.entity.Deposit;

import java.util.List;

@Dao
public interface DepositDao {
    @Insert
    void insert(Deposit deposit);

    @Update
    void update(Deposit deposit);

    @Delete
    void delete(Deposit deposit);

    @Query("SELECT * FROM deposits WHERE tenantId = :tenantId ORDER BY date DESC")
    LiveData<List<Deposit>> getDepositsForTenant(int tenantId);

    @Query("SELECT * FROM deposits WHERE tenantId = :tenantId")
    List<Deposit> getDepositsForTenantSync(int tenantId);
}
