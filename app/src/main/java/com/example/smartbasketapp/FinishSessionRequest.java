package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class FinishSessionRequest {
    @SerializedName("user_id")
    private int userId;
    @SerializedName("total")
    private double total;
    public FinishSessionRequest(int userId, double total) {
        this.userId = userId;
        this.total = total;
    }
}
