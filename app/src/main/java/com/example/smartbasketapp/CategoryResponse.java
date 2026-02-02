package com.example.smartbasketapp;

import android.icu.util.ULocale;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CategoryResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("categories")
    private List<Category> categories;

    public String getStatus() {
        return status;
    }

    public List<Category> getCategories() {
        return categories;
    }
}