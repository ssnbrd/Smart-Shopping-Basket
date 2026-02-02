package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class ConnectBasketResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("ID_session")
    private int sessionId;

    public String getStatus() { return status; }
    public int getSessionId() { return sessionId; }
}