package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class UpdateQuantityRequest {

    @SerializedName("list_id")
    private int listId;

    @SerializedName("product_id")
    private int productId;

    @SerializedName("new_quantity")
    private int newQuantity;

    public UpdateQuantityRequest(int listId, int productId, int newQuantity) {
        this.listId = listId;
        this.productId = productId;
        this.newQuantity = newQuantity;
    }
}