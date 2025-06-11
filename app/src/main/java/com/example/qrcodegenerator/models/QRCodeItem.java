package com.example.qrcodegenerator.models;

import java.util.Date;

public class QRCodeItem {
    private String id;
    private String userId;
    private String type;
    private String data;
    private Date timestamp;

    public QRCodeItem() {
        // Required for Firestore
    }

    public QRCodeItem(String id, String userId, String type, String data, Date timestamp) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.data = data;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}