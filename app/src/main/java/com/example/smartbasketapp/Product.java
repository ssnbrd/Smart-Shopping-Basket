package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient boolean isFavorite = false;
    @SerializedName("ID_product")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("price")
    private double price;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName(value = "description", alternate = {"Description"})
    private String description;

    public Product() {}

    public Product(int id, String name, double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }

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
    public String getDescription() {
        return description;
    }
    public boolean isFavorite() {
        return isFavorite;
    }
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
