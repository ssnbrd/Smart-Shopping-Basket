package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class UpdateCheckedRequest {

    @SerializedName("list_id")
    private int listId;

    @SerializedName("product_id")
    private int productId;

    @SerializedName("is_checked")
    private boolean isChecked;

    public UpdateCheckedRequest(int listId, int productId, boolean isChecked) {
        this.listId = listId;
        this.productId = productId;
        this.isChecked = isChecked;
    }
}