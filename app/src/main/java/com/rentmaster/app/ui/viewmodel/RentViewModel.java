package com.rentmaster.app.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.rentmaster.app.data.AppDatabase;
import com.rentmaster.app.data.dao.RentRecordDao;
import com.rentmaster.app.data.dao.RoomDao;
import com.rentmaster.app.data.dao.TenantDao;
import com.rentmaster.app.data.entity.Payment;
import com.rentmaster.app.data.entity.RentRecord;
import com.rentmaster.app.data.entity.RentRecordWithDetails;
import com.rentmaster.app.data.entity.Room;
import com.rentmaster.app.data.entity.Tenant;
import com.rentmaster.app.data.repository.PaymentRepository;
import com.rentmaster.app.data.repository.PropertyRepository;
import com.rentmaster.app.data.repository.RentRepository;
import com.rentmaster.app.notification.NotificationHelper;

import java.util.Calendar;
import java.util.List;

public class RentViewModel extends AndroidViewModel {
    private final RentRepository rentRepository;
    private final PaymentRepository paymentRepository;
    private final TenantDao tenantDao;
    private final RoomDao roomDao;
    private final RentRecordDao rentRecordDao;

    public RentViewModel(@NonNull Application application) {
        super(application);
        rentRepository = new RentRepository(application);
        paymentRepository = new PaymentRepository(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        tenantDao = db.tenantDao();
        roomDao = db.roomDao();
        rentRecordDao = db.rentRecordDao();
    }

    public LiveData<Double> getTotalPaid() {
        return rentRepository.getTotalPaid();
    }

    public LiveData<List<RentRecordWithDetails>> getRentHistoryForTenantWithDetails(int tenantId) {
        return rentRepository.getRentHistoryForTenantWithDetails(tenantId);
    }

    public LiveData<List<RentRecordWithDetails>> getRentHistoryForRoomWithDetails(int roomId) {
        return rentRepository.getRentHistoryForRoomWithDetails(roomId);
    }

    public void insert(RentRecord record) {
        rentRepository.insert(record);
    }

    public void update(RentRecord record) {
        rentRepository.update(record);
    }

    public LiveData<List<RentRecordWithDetails>> getAllRentRecordsWithDetails() {
        return rentRepository.getAllRentRecordsWithDetails();
    }

    public LiveData<RentRecord> getRentRecordById(int id) {
        return rentRepository.getRentRecordById(id);
    }

    public void addPayment(Payment payment, RentRecord record) {
        PropertyRepository.databaseWriteExecutor.execute(() -> {
            paymentRepository.insert(payment);

            double amountPaid = record.amountPaid + payment.amount;

            record.amountPaid = amountPaid;
            if (amountPaid >= record.amountDue) {
                record.status = "Paid";
            } else {
                record.status = "Partial";
            }
            rentRepository.update(record);

            Tenant tenant = tenantDao.getTenantByIdSync(record.tenantId);
            Room room = roomDao.getRoomByIdSync(record.roomId);

            if (tenant != null && room != null) {
                NotificationHelper.notifyTenantPaymentReceived(getApplication(), tenant, payment.amount,
                        room.roomNumber);
            }

            NotificationHelper.cancelAllReminders(getApplication(), record.tenantId);

            // The user requested NOT to shift credit to other cards.
            // So we just keep the overpaid amount in this record.
            // Notifications and reminders for next month will be handled by the
            // PeriodicRentWorker or manual creation.
        });
    }

    public void delete(RentRecord record) {
        rentRepository.delete(record);
    }

    private RentRecord createNewRentRecord(Tenant tenant, Room room, RentRecord lastRecord) {
        int frequencyDays = "Weekly".equals(tenant.rentFrequency) ? 7 : 14;
        Calendar newPeriodStart = Calendar.getInstance();
        newPeriodStart.setTimeInMillis(lastRecord.periodEnd);
        if (lastRecord.periodEnd != 0) {
            newPeriodStart.add(Calendar.DAY_OF_YEAR, 1);
        } else {
            // fallback if periodEnd is 0
            newPeriodStart.setTimeInMillis(System.currentTimeMillis());
        }

        Calendar newPeriodEnd = (Calendar) newPeriodStart.clone();
        newPeriodEnd.add(Calendar.DAY_OF_YEAR, frequencyDays - 1);

        int m = newPeriodStart.get(Calendar.MONTH);
        int y = newPeriodStart.get(Calendar.YEAR);

        double rentAmount = room.baseRent;
        if ("Fortnightly".equalsIgnoreCase(tenant.rentFrequency)) {
            rentAmount = room.baseRent * 2;
        } else if ("Daily".equalsIgnoreCase(tenant.rentFrequency)) {
            rentAmount = room.baseRent / 7.0;
        }

        return new RentRecord(tenant.id, room.id, m, y, rentAmount, newPeriodEnd.getTimeInMillis(),
                newPeriodStart.getTimeInMillis(), newPeriodEnd.getTimeInMillis());
    }
}
