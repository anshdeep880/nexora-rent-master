package com.rentmaster.app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.rentmaster.app.data.AppDatabase;
import com.rentmaster.app.data.dao.PaymentDao;
import com.rentmaster.app.data.dao.RentRecordDao;
import com.rentmaster.app.data.dao.RoomDao;
import com.rentmaster.app.data.dao.TenantDao;
import com.rentmaster.app.data.entity.RentRecord;
import com.rentmaster.app.data.entity.Tenant;
import com.rentmaster.app.data.model.TenantReport;
import com.rentmaster.app.notification.NotificationHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class TenantRepository {
    private TenantDao tenantDao;
    private RoomDao roomDao;
    private PaymentDao paymentDao;
    private AppDatabase db;
    private RentRecordDao rentRecordDao;
    private Application application;

    public TenantRepository(Application application) {
        this.application = application;
        db = AppDatabase.getDatabase(application);
        tenantDao = db.tenantDao();
        roomDao = db.roomDao();
        paymentDao = db.paymentDao();
        rentRecordDao = db.rentRecordDao();
    }

    public LiveData<List<Tenant>> getAllActiveTenants() {
        return tenantDao.getAllActiveTenants();
    }

    public LiveData<List<Tenant>> getTenantsByCategory(boolean active) {
        return tenantDao.getTenantsByCategory(active);
    }

    public LiveData<Tenant> getTenantForRoom(int roomId) {
        return tenantDao.getTenantForRoom(roomId);
    }

    public LiveData<Tenant> getTenantById(int id) {
        return tenantDao.getTenantById(id);
    }

    public LiveData<List<TenantReport>> getTenantsWithTotalPayments() {
        MediatorLiveData<List<TenantReport>> tenantReports = new MediatorLiveData<>();

        Runnable refreshReports = () -> {
            PropertyRepository.databaseWriteExecutor.execute(() -> {
                List<Tenant> tenants = tenantDao.getAllTenantsSync();
                List<TenantReport> reports = new ArrayList<>();
                for (Tenant tenant : tenants) {
                    double totalPayments = rentRecordDao.getTotalPaidForTenantSync(tenant.id);
                    String roomNumber = roomDao.getRoomNumberForTenantSync(tenant.id);
                    reports.add(new TenantReport(tenant.name, roomNumber, totalPayments));
                }
                tenantReports.postValue(reports);
            });
        };

        tenantReports.addSource(tenantDao.getAllTenants(), tenants -> refreshReports.run());
        tenantReports.addSource(rentRecordDao.getTotalPaid(), total -> refreshReports.run());

        return tenantReports;
    }

    public void insert(Tenant tenant) {
        PropertyRepository.databaseWriteExecutor.execute(() -> tenantDao.insert(tenant));
    }

    public void insertWithInitialRent(Tenant tenant, double baseRent) {
        PropertyRepository.databaseWriteExecutor.execute(() -> {
            long id = tenantDao.insert(tenant);

            Calendar pointerCal = Calendar.getInstance();
            pointerCal.setTimeInMillis(tenant.startDate);
            long currentTime = System.currentTimeMillis();

            do {
                long periodStart = pointerCal.getTimeInMillis();
                Calendar endCal = (Calendar) pointerCal.clone();
                int frequencyDays = "Weekly".equals(tenant.rentFrequency) ? 7 : 14;
                endCal.add(Calendar.DAY_OF_YEAR, frequencyDays - 1);
                long periodEnd = endCal.getTimeInMillis();

                Calendar pCal = Calendar.getInstance();
                pCal.setTimeInMillis(periodStart);
                int m = pCal.get(Calendar.MONTH);
                int y = pCal.get(Calendar.YEAR);

                double rentAmount = baseRent;
                if ("Fortnightly".equalsIgnoreCase(tenant.rentFrequency)) {
                    rentAmount = baseRent * 2;
                } else if ("Daily".equalsIgnoreCase(tenant.rentFrequency)) {
                    rentAmount = baseRent / 7.0;
                }

                // FIXED: Due date should be periodEnd (day 7 for weekly, day 14 for fortnightly)
                RentRecord newRecord = new RentRecord((int) id, tenant.roomId, m, y, rentAmount, periodEnd, periodStart, periodEnd);
                AppDatabase db = AppDatabase.getDatabase(application);
                db.rentRecordDao().insert(newRecord);

                String roomNumber = roomDao.getRoomNumberForTenantSync((int) id);
                if (roomNumber == null) roomNumber = "Unknown";

                // FIXED: Check if due date is in the past to determine which reminders to schedule
                if (newRecord.dueDate < System.currentTimeMillis()) {
                    // Past due date - schedule overdue reminder (2 days after)
                    NotificationHelper.scheduleOverdueRentReminders(application, tenant, newRecord, roomNumber);
                } else {
                    // Future due date - schedule pre-due reminders (2 days before, on due date)
                    NotificationHelper.schedulePreDueDateReminders(application, tenant, newRecord, roomNumber);
                }

                pointerCal.add(Calendar.DAY_OF_YEAR, frequencyDays);
            } while (pointerCal.getTimeInMillis() <= currentTime);

            // FIXED: Send welcome messages only once, outside the loop
            String roomNumber = roomDao.getRoomNumberForTenantSync((int) id);
            if (roomNumber == null)
                roomNumber = "Unknown";

            double initialRent = baseRent;
            if ("Fortnightly".equalsIgnoreCase(tenant.rentFrequency)) {
                initialRent = baseRent * 2;
            } else if ("Daily".equalsIgnoreCase(tenant.rentFrequency)) {
                initialRent = baseRent / 7.0;
            }

            // Notify landlord about new tenant immediately
            NotificationHelper.notifyLandlordNewTenant(application, tenant, roomNumber, initialRent);
            // Notify tenant with welcome message
            NotificationHelper.notifyTenantWelcome(application, tenant, roomNumber);
        });
    }

    public void update(Tenant tenant) {
        PropertyRepository.databaseWriteExecutor.execute(() -> tenantDao.update(tenant));
    }

    public void delete(Tenant tenant) {
        PropertyRepository.databaseWriteExecutor.execute(() -> tenantDao.delete(tenant));
    }
}
