package com.xsyin.opkey;

import android.support.v7.app.AppCompatActivity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import iie.dcs.crypto.Crypto;
import iie.dcs.utils.StringUtils;

public class ManualActivity extends AppCompatActivity {

    private Crypto mCrypto = Crypto.getInstance();

    private Button mPubKeyBtn = null;
    private TextView mMsgText = null;
    Contact contact = new Contact();
    private Button UpdateBtn = null;
    private Button CopyBtn = null;
    private EditText ePhone = null, pk = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        mMsgText = (TextView) findViewById(R.id.msg_text);
        mPubKeyBtn = (Button) findViewById(R.id.get_pub_key_btn);
        UpdateBtn = (Button) findViewById(R.id.update_contact);
        CopyBtn = (Button) findViewById(R.id.copy_public_key);
        ePhone = (EditText) findViewById(R.id.phone_number);
        pk = (EditText) findViewById(R.id.public_key);
        UpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pk.getText().toString().startsWith("04")) {
                    if (PhoneNumberUtils.isGlobalPhoneNumber(ePhone.getText().toString())) {
                        if (ActivityCompat.checkSelfPermission(ManualActivity.this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                            ActivityCompat.requestPermissions(ManualActivity.this, new String[]{Manifest.permission.WRITE_CONTACTS}, 2);
                        else
                            updateContact();
                    } else {
                        Toast.makeText(ManualActivity.this, "手机号码错误", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ManualActivity.this, "公钥有误", Toast.LENGTH_SHORT).show();
                }
            }
        });


        CopyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myPK = mMsgText.getText().toString();
                if (myPK.startsWith("04")) {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("my public key", myPK);
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(ManualActivity.this, "复制成功！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ManualActivity.this, "内容非公钥，复制失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //连接安全核心服务
        if (!mCrypto.ConnectSecureCore(ManualActivity.this)) {
            mMsgText.setText("尚未安装安全核心APP,无法连接服务");
            return;
        }

        mPubKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCrypto.isReady()) {
                    mMsgText.setText("安全核心APP尚未连接");
                    return;
                }
                byte[] pubKey = mCrypto.getPublicKey();
                if (pubKey == null) {
                    mMsgText.setText("从安全核心获取公钥失败");
                    return;
                }
                String s = StringUtils.bytesToHexString(pubKey);
                mMsgText.setText(s);
                if (!mCrypto.isReady()) {
                    mMsgText.setText("安全核心APP尚未连接");
                    return;
                }

//                byte[] data=new byte[]{1,2,3}; //待签名的数据
//                byte[] sig=mCrypto.hashAndSignData(pubKey);
//                if(sig==null){
//                    mMsgText.setText("安全核心签名失败");
//                    return;
//                }
            }
        });

    }


    public void updateContact() {
        String phoneNumber = ePhone.getText().toString();
        String publicKey = pk.getText().toString();
        //String contactNumber = phoneNumber.substring(0,1)+" "+phoneNumber.substring(1,4)+"-"+phoneNumber.substring(4,7)+"-"+phoneNumber.substring(7,11);
        String contactNumber = phoneNumber.substring(0, 3) + " " + phoneNumber.substring(3, 7) + " " + phoneNumber.substring(7, 11);
        contact.setPhoneNumber(contactNumber);
        ContactsManager contactsManager = new ContactsManager(ManualActivity.this.getContentResolver());
        contact = contactsManager.searchContactByNumber(contact);
        contact.setRemarks(publicKey);
        if (contactsManager.updateContact(contact)) {
            Toast.makeText(ManualActivity.this, "写入成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ManualActivity.this, "写入失败，请重试", Toast.LENGTH_SHORT).show();
            return;
        }

        return;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    updateContact();
                else
                    Toast.makeText(this, "通讯录权限已禁止", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCrypto != null)
            mCrypto.DisconnectService();
    }


}
