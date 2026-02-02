package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class AddToListRequest {
    @SerializedName("list_id")
    private int listId;
    @SerializedName("product_id")
    private int productId;

    @SerializedName("required_quantity")
    private int requiredQuantity;

    public AddToListRequest(int listId, int productId, int requiredQuantity) {
        this.listId = listId;
        this.productId = productId;
        this.requiredQuantity = requiredQuantity;
    }
    public int getListId() {
        return listId;
    }
    public int getProductId() {

        return productId;
    }

    public int getRequiredQuantity() {

        return requiredQuantity;
    }
}