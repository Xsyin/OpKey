package com.xsyin.opkey;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";

    private TextView other_pk = null, other_phone = null;

    public  Contact contact = new Contact();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Intent intent = getIntent();
        other_phone = (TextView)findViewById(R.id.others_nu);
        other_pk = (TextView)findViewById(R.id.others_pkey);
        Button updateBtn = (Button)findViewById(R.id.update_by_sms);


        String phoneNumber = intent.getStringExtra("other_phone");
        String publicKey = intent.getStringExtra("other_public_key");
        if (phoneNumber != null && publicKey != null){
            String contactNumber = phoneNumber.substring(0,3)+" "+phoneNumber.substring(3,7)+" "+phoneNumber.substring(7,11);
//            String contactNumber = phoneNumber.substring(0,1)+" "+phoneNumber.substring(1,4)+"-"+phoneNumber.substring(4,7)+"-"+phoneNumber.substring(7,11);

            contact.setPhoneNumber(contactNumber);
            contact.setRemarks(publicKey);
            other_phone.setText(phoneNumber);
            other_pk.setText(publicKey);
        }

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oPK = other_pk.getText().toString().trim();
                if (oPK.startsWith("04")){
                    if (ActivityCompat.checkSelfPermission(NotificationActivity.this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(NotificationActivity.this,new String[]{Manifest.permission.WRITE_CONTACTS},2);
                    else
                        updateContactBySMS();
                }else{
                    createDialog("公钥有误");
                }
            }
        });
    }

    public void updateContactBySMS(){
        Log.d(TAG, "onClick: "+contact.getRemarks());
        Log.d(TAG, "onClick: "+contact.getPhoneNumber());
        ContactsManager contactsManager = new ContactsManager(NotificationActivity.this.getContentResolver());
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
            case 2:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    updateContactBySMS();
                else
                    Toast.makeText(this, "通讯录权限已禁止", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
    }
}
