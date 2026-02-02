package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class ConnectBasketRequest {
    @SerializedName("qr_code")
    private String qrCode;
    @SerializedName("user_id")
    private int userId;

    public ConnectBasketRequest(String qrCode, int userId) {
        this.qrCode = qrCode;
        this.userId = userId;
    }
}