package com.example.smartbasketapp;

public class ProfileOption {
    private String title;
    private boolean isHighlighted;

    public ProfileOption(String title, boolean isHighlighted) {
        this.title = title;
        this.isHighlighted = isHighlighted;
    }

    public String getTitle() {
        return title;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }
}