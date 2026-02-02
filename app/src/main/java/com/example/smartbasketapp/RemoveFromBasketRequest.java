package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class RemoveFromBasketRequest {

    @SerializedName("product_id")
    private int productId;

    public RemoveFromBasketRequest(int productId) {
        this.productId = productId;
    }
}