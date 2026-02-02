package com.example.smartbasketapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;
public class ListContentResponse {

    @SerializedName("status")
    private String status;
    @SerializedName("content")
    private List<ListContentItem> content;

    public String getStatus() {
        return status;
    }

    public List<ListContentItem> getContent() {
        return content;
    }
}