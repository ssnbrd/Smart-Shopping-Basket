package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class TransactionDetail {
    @SerializedName("ID_product")
    private int productId;
    @SerializedName("name")
    private String name;
    @SerializedName("price")
    private double price;
    @SerializedName("checkedQuantity")
    private int quantity;

    public int getProductId() {
        return productId;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }
    public int getQuantity() {
        return quantity;
    }
}