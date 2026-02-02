package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class RemoveFromFavoritesRequest {
    @SerializedName("user_id")
    private int userId;
    @SerializedName("product_id")
    private int productId;

    public RemoveFromFavoritesRequest(int userId, int productId) {
        this.userId = userId;
        this.productId = productId;
    }
}

//public class RemoveFromFavoritesRequest {
//    @SerializedName("user_id")
//    private int userId;
//    @SerializedName("product_id")
//    private int productId;
//
//    public RemoveFromFavoritesRequest(int userId, int productId) {
//        this.userId = userId;
//        this.productId = productId;
//    }
//
//    public int getUserId() {
//        return userId;
//    }
//
//    public int getProductId() {
//        return productId;
//    }
//}