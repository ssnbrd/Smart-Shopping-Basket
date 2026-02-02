package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class ListContentItem {

    @SerializedName("ID_product")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("price")
    private double price;
    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("requiredQuantity")
    private int requiredQuantity;
    @SerializedName("is_checked")
    private boolean isChecked;

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public int getRequiredQuantity() { return requiredQuantity; }
    public boolean isChecked() { return isChecked; }
}