package com.example.smartbasketapp;

import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.apply(this);
    }
}
