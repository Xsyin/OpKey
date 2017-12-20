package com.xsyin.opkey;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by xsyin on 17-12-6.
 */

public class ContactsManager {
    private ContentResolver contentResolver;

    private static final String TAG = "ContactsManager";

    public ContactsManager(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public Contact searchContactByNumber(Contact contact){
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        Cursor  cursor = null;
        try {
            cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,ContactsContract.CommonDataKinds.Phone.NUMBER+"=?",new String[]{contact.getPhoneNumber()},null);
            if (cursor != null && cursor.moveToFirst()) {
               do {
                   String id = cursor.getString(0);
                   contact.setId(id);
                   String name = cursor.getString(1);
                   contact.setName(name);
                   Log.d(TAG, "searchContactByNumber: ");
               }while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return contact;
    }

    public boolean updateContact(Contact contact){
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        Cursor noteCursor = null;
        try {
               noteCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                       new String[]{ContactsContract.Data.CONTACT_ID, ContactsContract.CommonDataKinds.Note.NOTE},
                       ContactsContract.Data.CONTACT_ID+"=?"+" AND "
               + ContactsContract.Data.MIMETYPE+"=?",
                       new String[]{contact.getId(), ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE},null);
            if (noteCursor != null && noteCursor.moveToFirst()) {
                String note = noteCursor.getString(noteCursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                String id = noteCursor.getString(noteCursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));

                ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(ContactsContract.Data.RAW_CONTACT_ID+"=? AND "
                                        + ContactsContract.Data.MIMETYPE+"=?",
                                new String[]{contact.getId(), ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE})
                        .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE,contact.getRemarks())
                        .build());
            }else{
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID,contact.getId())
                        .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE,contact.getRemarks())
                        .build());
            }
            contentResolver.applyBatch(ContactsContract.AUTHORITY,ops);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (noteCursor != null) {
                noteCursor.close();
            }
        }
        return  true;
    }


}

