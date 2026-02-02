package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CartContentResponse {

    @SerializedName("status")
    private String status;
    @SerializedName("content")
    private List<CartProduct> content;
    @SerializedName("ID_session")
    private int sessionId;
    public String getStatus() {
        return status;
    }
    public int getSessionId() {
        return sessionId;
    }
    public List<CartProduct> getContent() {
        return content;
    }
}
