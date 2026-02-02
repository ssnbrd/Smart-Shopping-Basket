package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class Product {
    private transient boolean isFavorite = false;
    @SerializedName("ID_product")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("price")
    private double price;
    @SerializedName("image_url")
    private String imageUrl;
    public Product() {}

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
    public boolean isFavorite() {
        return isFavorite;
    }
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

}