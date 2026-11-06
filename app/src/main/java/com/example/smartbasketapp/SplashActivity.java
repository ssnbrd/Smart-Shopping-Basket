package com.example.smartbasketapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        if (sessionManager.getAuthToken() != null && !sessionManager.getAuthToken().isEmpty()) {
            Intent main = new Intent(this, MainActivity.class);
            if (getIntent().getBooleanExtra(MainActivity.EXTRA_START_SCAN, false)) {
                main.putExtra(MainActivity.EXTRA_START_SCAN, true);
            }
            startActivity(main);
        } else {
            startActivity(new Intent(this, AuthActivity.class));
        }
        finish();
    }
}