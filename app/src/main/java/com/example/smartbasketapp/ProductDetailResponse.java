package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class ProductDetailResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("product")
    private Product product;

    public String getStatus() {
        return status;
    }

    public Product getProduct() {
        return product;
    }
}
