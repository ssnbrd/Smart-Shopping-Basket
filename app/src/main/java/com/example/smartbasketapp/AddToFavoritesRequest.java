package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class AddToFavoritesRequest {

    @SerializedName("user_id")
    private int userId;

    @SerializedName("product_id")
    private int productId;

    public AddToFavoritesRequest(int userId, int productId) {
        this.userId = userId;
        this.productId = productId;
    }
    public int getUserId() {
        return userId;
    }

    public int getProductId() {
        return productId;
    }
}