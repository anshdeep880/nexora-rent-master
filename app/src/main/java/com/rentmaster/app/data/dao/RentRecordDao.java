package com.rentmaster.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rentmaster.app.data.entity.RentRecord;
import com.rentmaster.app.data.entity.RentRecordWithDetails;

import java.util.List;

@Dao
public interface RentRecordDao {
    @Insert
    void insert(RentRecord rentRecord);

    @Update
    void update(RentRecord rentRecord);

    @androidx.room.Delete
    void delete(RentRecord rentRecord);

    @Query("SELECT r.*, t.name as tenantName, o.roomNumber as roomNumber, t.isActive as isActive, p.id as propertyId, p.name as propertyName FROM rent_records r JOIN tenants t ON r.tenantId = t.id JOIN rooms o ON r.roomId = o.id JOIN properties p ON o.propertyId = p.id WHERE r.tenantId = :tenantId ORDER BY t.isActive DESC, r.periodStart DESC")
    LiveData<List<RentRecordWithDetails>> getRentHistoryForTenantWithDetails(int tenantId);

    @Query("SELECT * FROM rent_records WHERE tenantId = :tenantId ORDER BY periodStart DESC")
    List<RentRecord> getRentHistoryForTenantSync(int tenantId);

    @Query("SELECT * FROM rent_records WHERE tenantId = :tenantId ORDER BY periodStart DESC LIMIT 1")
    RentRecord getLatestRentRecordForTenantSync(int tenantId);

    @Query("SELECT * FROM rent_records WHERE status != 'Paid'")
    LiveData<List<RentRecord>> getAllUnpaidRecords();

    @Query("SELECT r.*, t.name as tenantName, o.roomNumber as roomNumber, t.isActive as isActive, p.id as propertyId, p.name as propertyName FROM rent_records r JOIN tenants t ON r.tenantId = t.id JOIN rooms o ON r.roomId = o.id JOIN properties p ON o.propertyId = p.id ORDER BY t.isActive DESC, r.periodStart DESC, t.name ASC")
    LiveData<List<RentRecordWithDetails>> getAllRentRecordsWithDetails();

    @Query("SELECT * FROM rent_records WHERE tenantId = :tenantId AND month = :month AND year = :year LIMIT 1")
    RentRecord getRentRecordSync(int tenantId, int month, int year);

    @Query("SELECT * FROM rent_records WHERE roomId = :roomId AND month = :month AND year = :year LIMIT 1")
    RentRecord getRentRecordForRoomSync(int roomId, int month, int year);

    @Query("SELECT r.*, t.name as tenantName, o.roomNumber as roomNumber, t.isActive as isActive, p.id as propertyId, p.name as propertyName FROM rent_records r JOIN tenants t ON r.tenantId = t.id JOIN rooms o ON r.roomId = o.id JOIN properties p ON o.propertyId = p.id WHERE r.roomId = :roomId ORDER BY t.isActive DESC, r.periodStart DESC")
    LiveData<List<RentRecordWithDetails>> getRentHistoryForRoomWithDetails(int roomId);

    @Query("SELECT * FROM rent_records WHERE id = :id LIMIT 1")
    LiveData<RentRecord> getRentRecordById(int id);

    @Query("SELECT SUM(amountPaid) FROM rent_records")
    LiveData<Double> getTotalPaid();

    @Query("SELECT SUM(amountPaid) FROM rent_records WHERE tenantId = :tenantId")
    double getTotalPaidForTenantSync(int tenantId);

    // New queries for period-based logic and reports
    @Query("SELECT * FROM rent_records WHERE tenantId = :tenantId AND periodStart = :periodStart LIMIT 1")
    RentRecord getRentRecordByPeriodSync(int tenantId, long periodStart);

    @Query("SELECT * FROM rent_records WHERE roomId = :roomId AND periodStart = :periodStart LIMIT 1")
    RentRecord getRentRecordForRoomByPeriodSync(int roomId, long periodStart);

    @Query("SELECT SUM(amountDue - amountPaid) FROM rent_records WHERE roomId = :roomId AND (status = 'Unpaid' OR status = 'Partial')")
    double getOutstandingRentForRoomSync(int roomId);

    @Query("SELECT SUM(amountDue - amountPaid) FROM rent_records WHERE tenantId = :tenantId")
    LiveData<Double> getOutstandingRentForTenant(int tenantId);
}
