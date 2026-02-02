package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransactionResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("transactions")
    private List<Transaction> transactions;

    public String getStatus() { return status; }
    public List<Transaction> getTransactions() { return transactions; }
}