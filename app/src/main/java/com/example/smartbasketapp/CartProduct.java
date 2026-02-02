package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class CartProduct {
    @SerializedName("ID_product")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("price")
    private double price;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("scannedQuantity")
    private int scannedQuantity;
    public CartProduct() {}

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public int getScannedQuantity() {
        return scannedQuantity;
    }
}