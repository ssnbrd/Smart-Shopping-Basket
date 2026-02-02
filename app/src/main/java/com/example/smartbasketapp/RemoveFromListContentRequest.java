package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class RemoveFromListContentRequest {

    @SerializedName("list_id")
    private int listId;

    @SerializedName("product_id")
    private int productId;

    public RemoveFromListContentRequest(int listId, int productId) {
        this.listId = listId;
        this.productId = productId;
    }
}