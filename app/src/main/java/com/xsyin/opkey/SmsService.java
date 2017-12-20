package com.xsyin.opkey;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class SmsService extends Service {

    private Context context;
    private String sender = null;
    private String publicKey = null;

    private SmsReceiver smsReceiver = new SmsReceiver(new SmsListener(){
        @Override
        public void onResult(String smsContent, String phoneNumber) {
            sender = phoneNumber;
            publicKey = smsContent;
            createNotification();
        }
    });

    private static final String TAG = "SmsService";

    
    public SmsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        context = getApplicationContext();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(smsReceiver,filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: service");
        unregisterReceiver(smsReceiver);
        stopSelf();
    }

    public void createNotification(){

        Intent intent = new Intent(this,NotificationActivity.class);
        intent.putExtra("other_phone",sender);
        intent.putExtra("other_public_key",publicKey);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("OpKey")
                .setContentText("新的公钥")
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pi);

        notificationManager.notify(1,notification.build());

    }

}
