package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FavoritesResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("favorites")
    private List<Product> favorites;

    public String getStatus() {
        return status;
    }

    public List<Product> getFavorites() {
        return favorites;
    }
}