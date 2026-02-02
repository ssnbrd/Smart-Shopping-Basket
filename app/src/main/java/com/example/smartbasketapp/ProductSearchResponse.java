package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProductSearchResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("products")
    private List<Product> products;

    public String getStatus() {
        return status;
    }

    public List<Product> getProducts() {
        return products;
    }
}