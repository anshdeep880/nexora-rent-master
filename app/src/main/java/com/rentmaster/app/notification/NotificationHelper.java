package com.rentmaster.app.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.rentmaster.app.data.entity.RentRecord;
import com.rentmaster.app.data.entity.Tenant;
import com.rentmaster.app.util.FormatUtils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationHelper {
    private static final int REQUEST_CODE_BASE_OVERDUE = 1000;
    private static final int REQUEST_CODE_BASE_PRE_DUE = 2000;
    private static final int REQUEST_CODE_BASE_DUE_DATE = 3000;

    public static void notifyLandlordNewTenant(Context context, Tenant tenant, String roomNumber, double amount) {
        String message = "New Tenant Added:\nRoom: " + roomNumber + 
                "\nTenant: " + tenant.name + 
                "\nContact: " + tenant.phone + 
                "\nAmount: " + FormatUtils.formatCurrency(amount);
        
        sendNotification(context, tenant, message, true, false);
    }

    public static void notifyTenantWelcome(Context context, Tenant tenant, String roomNumber) {
        String message = "Welcome to your new home!\nRoom: " + roomNumber + 
                "\nTenant: " + tenant.name + 
                "\nRent Master is here to manage your stay comfortably.";
        
        sendNotification(context, tenant, message, false, true);
    }

    public static void notifyTenantPaymentReceived(Context context, Tenant tenant, double amount, String roomNumber) {
        String message = "Payment Received Confirmation:\nRoom: " + roomNumber + 
                "\nTenant: " + tenant.name + 
                "\nAmount: " + FormatUtils.formatCurrency(amount) + 
                "\nThank you for your payment!";
        
        sendNotification(context, tenant, message, true, true);
    }

    public static void scheduleOverdueRentReminders(Context context, Tenant tenant, RentRecord record, String roomNumber) {
        String message = "Rent OVERDUE Alert!\nRoom: " + roomNumber + 
                "\nTenant: " + tenant.name + 
                "\nContact: " + tenant.phone + 
                "\nAmount: " + FormatUtils.formatCurrency(record.amountDue);

        // Schedule for 2 days after period start if not paid
        scheduleOneTimeReminder(context, tenant, message, record.periodStart, 2, REQUEST_CODE_BASE_OVERDUE + tenant.id, true, true);
    }

    public static void schedulePreDueDateReminders(Context context, Tenant tenant, RentRecord nextRecord, String roomNumber) {
        String baseMessage = "Rent Due Reminder:\nRoom: " + roomNumber + 
                "\nTenant: " + tenant.name + 
                "\nContact: " + tenant.phone + 
                "\nAmount: " + FormatUtils.formatCurrency(nextRecord.amountDue);

        // 2 days before period start
        scheduleOneTimeReminder(context, tenant, baseMessage + "\nStatus: Due in 2 days",
                nextRecord.periodStart, -2, REQUEST_CODE_BASE_PRE_DUE + tenant.id, true, true);

        // On period start date
        scheduleOneTimeReminder(context, tenant, baseMessage + "\nStatus: Due today",
                nextRecord.periodStart, 0, REQUEST_CODE_BASE_DUE_DATE + tenant.id, true, true);
    }

    private static void scheduleDailyReminder(Context context, Tenant tenant, String message, int requestCode) {
        // Removed as per request for specific intervals
    }

    private static void scheduleOneTimeReminder(Context context, Tenant tenant, String message, long dueDate, int dayOffset, int requestCode, boolean toLandlord, boolean toTenant) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = createIntent(context, tenant, message, toLandlord, toTenant);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        long triggerTime = getAdjustedTime(dueDate, dayOffset, 9);
        if (triggerTime > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        }
    }

    private static void sendNotification(Context context, Tenant tenant, String message, boolean toLandlord, boolean toTenant) {
        Intent intent = createIntent(context, tenant, message, toLandlord, toTenant);
        context.sendBroadcast(intent);
    }

    private static Intent createIntent(Context context, Tenant tenant, String message, boolean toLandlord, boolean toTenant) {
        Intent intent = new Intent(context, RentDueReceiver.class);
        intent.putExtra("TENANT_ID", tenant.id);
        intent.putExtra("TENANT_NAME", tenant.name);
        intent.putExtra("TENANT_PHONE", tenant.phone);
        intent.putExtra("MESSAGE", message);
        intent.putExtra("SEND_TO_LANDLORD", toLandlord);
        intent.putExtra("SEND_TO_TENANT", toTenant);
        return intent;
    }

    public static void cancelAllReminders(Context context, int tenantId) {
        cancelReminder(context, REQUEST_CODE_BASE_OVERDUE + tenantId);
        cancelReminder(context, REQUEST_CODE_BASE_PRE_DUE + tenantId);
        cancelReminder(context, REQUEST_CODE_BASE_DUE_DATE + tenantId);
    }

    private static void cancelReminder(Context context, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, RentDueReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private static long getAdjustedTime(long baseTime, int dayOffset, int hour) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(baseTime);
        cal.add(Calendar.DAY_OF_YEAR, dayOffset);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
