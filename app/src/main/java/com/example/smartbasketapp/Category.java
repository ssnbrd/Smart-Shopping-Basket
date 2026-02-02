package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("ID_category")
    private int id;
    @SerializedName("Name")
    private String name;

    public int getId() { return id; }
    public String getName() { return name; }
}
