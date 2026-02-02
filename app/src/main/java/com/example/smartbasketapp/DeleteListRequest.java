package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;
public class DeleteListRequest {
    @SerializedName("user_id")
    private int userId;

    public DeleteListRequest(int userId) {
        this.userId = userId;
    }
}
//public class DeleteListRequest {
//    @SerializedName("user_id")
//    private int userId;
//    public DeleteListRequest(int userId) {
//        this.userId = userId;
//    }
//    public int getUserId() {
//        return userId;
//    }
//}
