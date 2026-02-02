package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class CreateListRequest {

    @SerializedName("user_id")
    private int userId;

    @SerializedName("name")
    private String name;

    public CreateListRequest(int userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}