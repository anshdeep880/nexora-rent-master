package com.rentmaster.app.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.rentmaster.app.data.AppDatabase;
import com.rentmaster.app.data.entity.RentRecord;
import com.rentmaster.app.data.entity.Tenant;
import com.rentmaster.app.data.entity.Room;
import com.rentmaster.app.notification.NotificationHelper;

import java.util.Calendar;
import java.util.List;

public class PeriodicRentWorker extends Worker {

    public PeriodicRentWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        List<Tenant> activeTenants = db.tenantDao().getAllActiveTenantsSync();

        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();

        for (Tenant tenant : activeTenants) {
            if (tenant.roomId == null)
                continue;

            Room room = db.roomDao().getRoomSync(tenant.roomId);
            if (room == null)
                continue;

            int frequencyDays = "Weekly".equals(tenant.rentFrequency) ? 7 : 14;

            Calendar pointerCal = Calendar.getInstance();
            List<RentRecord> records = db.rentRecordDao().getRentHistoryForTenantSync(tenant.id);
            if (!records.isEmpty()) {
                pointerCal.setTimeInMillis(records.get(0).periodStart);
                pointerCal.add(Calendar.DAY_OF_YEAR, frequencyDays);
            } else {
                pointerCal.setTimeInMillis(tenant.startDate);
            }

            while (pointerCal.getTimeInMillis() < currentTime + (1000L * 60 * 60 * 24 * 7)) {
                long periodStart = pointerCal.getTimeInMillis();

                RentRecord existing = db.rentRecordDao().getRentRecordByPeriodSync(tenant.id, periodStart);
                if (existing == null) {
                    Calendar endCal = (Calendar) pointerCal.clone();
                    endCal.add(Calendar.DAY_OF_YEAR, frequencyDays - 1);
                    long periodEnd = endCal.getTimeInMillis();

                    Calendar pCal = Calendar.getInstance();
                    pCal.setTimeInMillis(periodStart);
                    int m = pCal.get(Calendar.MONTH);
                    int y = pCal.get(Calendar.YEAR);

                    double rentAmount = room.baseRent;
                    if ("Fortnightly".equalsIgnoreCase(tenant.rentFrequency)) {
                        rentAmount = room.baseRent * 2;
                    } else if ("Daily".equalsIgnoreCase(tenant.rentFrequency)) {
                        rentAmount = room.baseRent / 7.0;
                    }

                    // Set dueDate to periodEnd to match Repository logic
                    RentRecord newRecord = new RentRecord(tenant.id, tenant.roomId, m, y, rentAmount, periodEnd,
                            periodStart, periodEnd);
                    db.rentRecordDao().insert(newRecord);

                    if (newRecord.status.equals("Unpaid")) {
                        NotificationHelper.scheduleOverdueRentReminders(getApplicationContext(), tenant, newRecord,
                                room.roomNumber);
                    } else {
                        NotificationHelper.schedulePreDueDateReminders(getApplicationContext(), tenant, newRecord,
                                room.roomNumber);
                    }
                }
            }
        }

        return Result.success();
    }
}
