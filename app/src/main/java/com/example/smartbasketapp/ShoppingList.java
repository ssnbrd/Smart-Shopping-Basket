package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;

public class ShoppingList {
    @SerializedName("Name")
    private String name;
    @SerializedName("ID_list")
    private int id;
    public ShoppingList(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
    public void setName(String name) {
        this.name = name;
    }
}
