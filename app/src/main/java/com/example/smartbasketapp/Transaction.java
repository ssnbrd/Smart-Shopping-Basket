package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("ID_tranzaction")
    private int id;
    @SerializedName("totalAmount")
    private double totalAmount;
    @SerializedName("date_time")
    private String dateTime;

    public int getId() { return id; }
    public double getTotalAmount() { return totalAmount; }
    public String getDateTime() { return dateTime; }
}