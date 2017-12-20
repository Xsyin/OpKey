package com.xsyin.opkey;

/**
 * Created by xsyin on 17-12-6.
 */

public class Contact {
    private String id;
    private String name;
    private String phoneNumber;
    private String remarks;

    public Contact() {

    }

    public Contact(String id, String name, String phoneNumber, String remarks) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.remarks = remarks;
    }

    public Contact(Contact contact) {
        this.id = contact.id;
        this.name = contact.name;
        this.phoneNumber = contact.phoneNumber;
        this.remarks = contact.remarks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

}
