package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ShoppingListsResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("lists")
    private List<ShoppingList> lists;

    public String getStatus() {
        return status;
    }

    public List<ShoppingList> getLists() {
        return lists;
    }
}