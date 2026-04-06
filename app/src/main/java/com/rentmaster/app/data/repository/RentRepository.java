package com.rentmaster.app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.rentmaster.app.data.AppDatabase;
import com.rentmaster.app.data.dao.RentRecordDao;
import com.rentmaster.app.data.entity.RentRecord;
import com.rentmaster.app.data.entity.RentRecordWithDetails;

import java.util.List;

public class RentRepository {
    private RentRecordDao rentRecordDao;

    public RentRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        rentRecordDao = db.rentRecordDao();
    }

    public LiveData<List<RentRecordWithDetails>> getRentHistoryForTenantWithDetails(int tenantId) {
        return rentRecordDao.getRentHistoryForTenantWithDetails(tenantId);
    }

    public LiveData<List<RentRecordWithDetails>> getRentHistoryForRoomWithDetails(int roomId) {
        return rentRecordDao.getRentHistoryForRoomWithDetails(roomId);
    }

    public LiveData<List<RentRecord>> getAllUnpaidRecords() {
        return rentRecordDao.getAllUnpaidRecords();
    }

    public LiveData<List<RentRecordWithDetails>> getAllRentRecordsWithDetails() {
        return rentRecordDao.getAllRentRecordsWithDetails();
    }

    public LiveData<RentRecord> getRentRecordById(int id) {
        return rentRecordDao.getRentRecordById(id);
    }

    public LiveData<Double> getTotalPaid() {
        return rentRecordDao.getTotalPaid();
    }

    public void insert(RentRecord record) {
        PropertyRepository.databaseWriteExecutor.execute(() -> rentRecordDao.insert(record));
    }

    public void update(RentRecord record) {
        PropertyRepository.databaseWriteExecutor.execute(() -> rentRecordDao.update(record));
    }

    public void delete(RentRecord record) {
        PropertyRepository.databaseWriteExecutor.execute(() -> rentRecordDao.delete(record));
    }
}
