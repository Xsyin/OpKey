package com.xsyin.opkey;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import iie.dcs.crypto.Crypto;
import iie.dcs.utils.StringUtils;

public class MainActivity extends AppCompatActivity {

    private static final int READ_CONTACTS_REQUEST_CODE = 1;
    private static final int WRITE_CONTACTS_REQUEST_CODE = 2;
    private static final int SEND_SMS_REQUEST_CODE = 3;
    private static final String TAG = "MainActivity";
    private static boolean isStartService = false;
    private Crypto mCrypto= Crypto.getInstance();

    private Button mPubKeyBtn=null;
    private TextView mMsgText=null, myPhoneNumber = null;

    private Button UpdateBySmsBtn = null, CopyBtn = null;
    private TextView others_number = null, others_pk = null;
    private String name = null,number = null;
    private Uri uri = null;

    Contact contact = new Contact();

    private SmsReceiver mReceiver = new SmsReceiver(new SmsListener(){
        @Override
        public void onResult(String smsContent, String phoneNumber) {
//            String contactNumber = phoneNumber.substring(0,1)+" "+phoneNumber.substring(1,4)+"-"+phoneNumber.substring(4,7)+"-"+phoneNumber.substring(7,11);
            String contactNumber = phoneNumber.substring(0,3)+" "+phoneNumber.substring(3,7)+" "+phoneNumber.substring(7,11);
            contact.setPhoneNumber(contactNumber);
            contact.setRemarks(smsContent);
            others_number.setText(phoneNumber);
            others_pk.setText(smsContent);


        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isStartService){
            isStartService = false;
            Intent intent = new Intent(MainActivity.this,SmsService.class);
            stopService(intent);
        }

        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(mReceiver,filter);


        mMsgText = (TextView)findViewById(R.id.msg_text);
        myPhoneNumber = (TextView)findViewById(R.id.my_phone);
        others_number = (TextView)findViewById(R.id.others_nu);
        others_pk = (TextView)findViewById(R.id.others_pkey);
        mPubKeyBtn=(Button)findViewById(R.id.get_pub_key_btn);
        Button SendPKtoBtn = (Button)findViewById(R.id.send_pk_to);
        UpdateBySmsBtn = (Button)findViewById(R.id.update_by_sms);
        CopyBtn = (Button)findViewById(R.id.copy_public_key);

        CopyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myPK = mMsgText.getText().toString();
                if (myPK.startsWith("04")){
                    ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("my public key",myPK);
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(MainActivity.this, "复制成功！", Toast.LENGTH_SHORT).show();
                }else{
                    //Toast.makeText(MainActivity.this, "内容非公钥，复制失败", Toast.LENGTH_SHORT).show();
                    createDialog("内容非公钥，复制失败");
                }
            }
        });

        UpdateBySmsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String other_pk = others_pk.getText().toString().trim();
                Log.d(TAG, "onClick: " + other_pk);
                if (other_pk.startsWith("04")){
                        if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_CONTACTS},WRITE_CONTACTS_REQUEST_CODE);
                        else
                            updateContactBySMS();
                }else{
                    createDialog("公钥有误");
                }
            }
        });


        SendPKtoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myPK = mMsgText.getText().toString().trim();
                if (myPK.startsWith("04")){
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    startActivityForResult(intent,10);
                }else{
                    //Toast.makeText(MainActivity.this, "请检查内容是否为公钥", Toast.LENGTH_SHORT).show();
                    createDialog("请检查内容是否为公钥");
                }
                
            }
        });

       //连接安全核心服务
        if(!mCrypto.ConnectSecureCore(MainActivity.this)){
            mMsgText.setText("尚未安装安全核心APP,无法连接服务");
            return;
        }

        mPubKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mCrypto.isReady()){
                    mMsgText.setText("安全核心APP尚未连接");
                    return;
                }
                byte[] pubKey=mCrypto.getPublicKey();
                if(pubKey==null){
                    mMsgText.setText("从安全核心获取公钥失败");
                    return;
                }
                String s= StringUtils.bytesToHexString(pubKey);
                mMsgText.setText(s);
                if(!mCrypto.isReady()){
                    mMsgText.setText("安全核心APP尚未连接");
                    return;
                }

//                byte[] data=new byte[]{1,2,3}; //待签名的数据
//                byte[] sig=mCrypto.hashAndSignData(pubKey);
//                if(sig==null){
//                    mMsgText.setText("安全核心签名失败");
//                    return;
//                }

//                long rs=mCrypto.hashAndVerifyData(data,sig);
//                if(rs!=0){
//                    mMsgText.setText("安全核心验签失败");
//                    return;
//                }
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.manual_mode){
            Intent intent = new Intent(this,ManualActivity.class);
            startActivity(intent);
        }
        return true;
    }

    public void updateContactBySMS(){
        Log.d(TAG, "onClick: "+contact.getRemarks());
        Log.d(TAG, "onClick: "+contact.getPhoneNumber());
        ContactsManager contactsManager = new ContactsManager(MainActivity.this.getContentResolver());
        contact = contactsManager.searchContactByNumber(contact);
        if (contact.getId() == null){
            //Toast.makeText(this, "通讯录中不存在该联系人，请添加后重试", Toast.LENGTH_SHORT).show();
            createDialog("通讯录中不存在该联系人，请添加后重试");
        }else{
            if (contactsManager.updateContact(contact))
            {
                //Toast.makeText(MainActivity.this, "写入成功", Toast.LENGTH_SHORT).show();
                createDialog("写入成功");
            }else{
                //Toast.makeText(MainActivity.this, "写入失败，请重试", Toast.LENGTH_SHORT).show();
                createDialog("写入失败，请重试");
            }

        }
        return;
    }


    private void SendPKBySms() {
        Cursor cursor = this.getContentResolver().query(uri,null,null,null,null);
        final List<String> result = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()){
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            int phoneColumn = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
            int phoneNum = cursor.getInt(phoneColumn);
            if (phoneNum > 0) {
                int idColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);// 获得联系人的ID号
                String contactId = cursor.getString(idColumn);
                // 获得联系人电话的cursor
                Cursor phone = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
                if (phone != null && phone.moveToFirst()) {
                    for (; !phone.isAfterLast(); phone.moveToNext()) {
                        int index = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String phoneNumber = phone.getString(index);
                        result.add(phoneNumber);
                    }
                    if (!phone.isClosed()) {
                        phone.close();
                    }
                }
            }

        }
        if (result.size() == 1){
            number = result.get(0);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("公钥发送");
            builder.setMessage("使用短信发送自己的公钥给"+name+"\n"+number);
            builder.setCancelable(false);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS},SEND_SMS_REQUEST_CODE);
                    else
                        sendSms();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        if (result.size() > 1) {//如果号码多于2个，则弹出对话框让他选择
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            int size = result.size();
            builder.setTitle("短信发送公钥至");
            builder.setItems(result.toArray(new String[size]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    number = result.get(which);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS},SEND_SMS_REQUEST_CODE);
                    else
                        sendSms();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void sendSms(){
        String smsText = "我 的 public key: "+mMsgText.getText().toString();
        String sourceNumber = myPhoneNumber.getText().toString();
        Log.d(TAG, "sendSms: "+sourceNumber);
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> textList = smsManager.divideMessage(smsText);
        smsManager.sendMultipartTextMessage(number,null,textList,null,null);
        Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
    }

    public void createDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: "+grantResults.length);
        switch (requestCode){
            case READ_CONTACTS_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    SendPKBySms();
                else
                    Toast.makeText(this, "读通讯录权限已禁止", Toast.LENGTH_SHORT).show();
                break;
            case WRITE_CONTACTS_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    updateContactBySMS();
                else
                    Toast.makeText(this, "读通讯录权限已禁止", Toast.LENGTH_SHORT).show();
                break;
            case SEND_SMS_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    sendSms();
                else
                    Toast.makeText(this, "发送短信权限已禁止", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK) {
            if (data != null){
                uri = data.getData();
                if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_CONTACTS},READ_CONTACTS_REQUEST_CODE);
                else
                    SendPKBySms();
            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCrypto!=null)
            mCrypto.DisconnectService();
        unregisterReceiver(mReceiver);
        if (!isStartService){
            isStartService = true;
            Intent intent = new Intent(MainActivity.this,SmsService.class);
            startService(intent);
        }
    }


}
