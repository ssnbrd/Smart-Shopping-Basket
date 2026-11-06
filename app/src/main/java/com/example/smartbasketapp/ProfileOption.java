package com.example.smartbasketapp;

import androidx.annotation.StringRes;

public class ProfileOption {
    public static final int HISTORY = 1;
    public static final int FAVORITES = 2;
    public static final int SHOPPING_LISTS = 3;
    public static final int PAYMENT = 4;
    public static final int BONUS = 5;
    public static final int LANGUAGE = 6;
    public static final int ABOUT = 7;
    public static final int THEME = 8;

    private final int id;
    private final int titleRes;
    private final boolean isHighlighted;

    public ProfileOption(int id, @StringRes int titleRes, boolean isHighlighted) {
        this.id = id;
        this.titleRes = titleRes;
        this.isHighlighted = isHighlighted;
    }

    public int getId() {
        return id;
    }

    @StringRes
    public int getTitleRes() {
        return titleRes;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }
}
