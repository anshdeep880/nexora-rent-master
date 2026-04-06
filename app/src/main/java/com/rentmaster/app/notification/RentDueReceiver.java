package com.rentmaster.app.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import com.rentmaster.app.data.entity.Landlord;
import com.rentmaster.app.data.repository.LandlordRepository;
import android.app.Application;

public class RentDueReceiver extends BroadcastReceiver {
    private static final String TAG = "RentDueReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String tenantName = intent.getStringExtra("TENANT_NAME");
        String tenantPhone = intent.getStringExtra("TENANT_PHONE");
        String message = intent.getStringExtra("MESSAGE");
        boolean sendToLandlord = intent.getBooleanExtra("SEND_TO_LANDLORD", true);
        boolean sendToTenant = intent.getBooleanExtra("SEND_TO_TENANT", false);

        if (message == null)
            message = "Rent reminder for " + tenantName;

        final String finalMessage = message;

        // Fetch landlord details and send SMS
        new Thread(() -> {
            if (sendToTenant && tenantPhone != null && !tenantPhone.isEmpty()) {
                sendSms(tenantPhone, finalMessage);
            }

            if (sendToLandlord) {
                LandlordRepository repository = new LandlordRepository((Application) context.getApplicationContext());
                Landlord landlord = repository.getLandlordSync();

                if (landlord != null) {
                    sendSms(landlord.phone1, finalMessage);
                    sendSms(landlord.phone2, finalMessage);
                } else {
                    Log.e(TAG, "No landlord details found to send SMS");
                }
            }
        }).start();
    }

    private void sendSms(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isEmpty())
            return;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Log.d(TAG, "SMS sent to " + phoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS to " + phoneNumber, e);
        }
    }
}
