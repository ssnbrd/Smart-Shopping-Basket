package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class RenameListRequest {
    @SerializedName("user_id")
    private int userId;

    @SerializedName("new_name")
    private String newName;

    public RenameListRequest(int userId, String newName) {
        this.userId = userId;
        this.newName = newName;
    }

    public int getUserId() {
        return userId;
    }

    public String getNewName() {
        return newName;
    }

}
