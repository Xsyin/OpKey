package com.xsyin.opkey;

/**
 * Created by xsyin on 17-12-10.
 */

public interface SmsListener {
        void onResult(String smsContent, String phoneNumber);
}
