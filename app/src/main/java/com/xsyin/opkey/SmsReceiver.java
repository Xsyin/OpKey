package com.xsyin.opkey;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by xsyin on 17-12-16.
 */

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";
    public SmsListener smsListener;
    public String sender = null;
    public String publicKey = null;
    public SmsReceiver(SmsListener smsListener) {
        super();
        this.smsListener = smsListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        Bundle bundle = intent.getExtras();
        SmsMessage msg = null;
        if (null != bundle){
            Object[] smsObj = (Object[])bundle.get("pdus");
            for (Object object: smsObj){
                msg = SmsMessage.createFromPdu((byte[])object);
                sender = msg.getDisplayOriginatingAddress();
                String content = msg.getMessageBody();
                if (content.indexOf("public key:") != -1){
                    publicKey = content.split(":")[1];
                }else{
                    publicKey += content;
                }

            }
            if(sender.startsWith("+86")){
                sender = sender.substring(3,14);
            }
            if (!TextUtils.isEmpty(publicKey))
                smsListener.onResult(publicKey,sender);
        }

    }


}
