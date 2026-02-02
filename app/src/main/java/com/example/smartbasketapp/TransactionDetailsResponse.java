package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransactionDetailsResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("details")
    private List<TransactionDetail> details;

    public String getStatus() { return status; }
    public List<TransactionDetail> getDetails() { return details; }
}
