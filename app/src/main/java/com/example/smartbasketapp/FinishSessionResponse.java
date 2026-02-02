package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class FinishSessionResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("ID_transaction")
    private int transactionId;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public int getTransactionId() { return transactionId; }
}