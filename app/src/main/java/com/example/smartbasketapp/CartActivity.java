package com.example.smartbasketapp;

import android.content.Context;
import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CartActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private View emptyCartContainer;
    private EditText qrCodeEditText;
    private final ActivityResultLauncher<ScanOptions> qrCodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    connectToBasket(result.getContents());
                } else {
                    Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_LONG).show();
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        sessionManager = new SessionManager(this);
        emptyCartContainer = findViewById(R.id.empty_cart_container);

        if (sessionManager.getSessionId() != -1) {
            openActiveCartFragment();
        } else {
            emptyCartContainer.setVisibility(View.VISIBLE);
        }

        LinearLayout scanButton = findViewById(R.id.startShoppingButton);

        scanButton.setOnClickListener(v -> {
            startQrScanner();
        });

        qrCodeEditText = findViewById(R.id.qrCodeEditText);
        Button connectByCodeButton = findViewById(R.id.connectByCodeButton);

        connectByCodeButton.setOnClickListener(v -> {
            String qrCode = qrCodeEditText.getText().toString().trim();
            if (!qrCode.isEmpty()) {
                hideKeyboard();
                connectToBasket(qrCode);
            } else {
                Toast.makeText(this, "Введите код корзины", Toast.LENGTH_SHORT).show();
            }
        });

        BottomNavigationView navView = findViewById(R.id.bottomNavigation);

        navView.setSelectedItemId(R.id.navigation_cart);

        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(CartActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_search) {
                startActivity(new Intent(CartActivity.this, SearchActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(CartActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_cart) {
                return true;
            }

            return false;
        });
    }
    private void startQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(true);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Наведите камеру на QR-код");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true);

        qrCodeLauncher.launch(options);
    }
    private void connectToBasket(String qrCode) {
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();

        if (userId == -1) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        ConnectBasketRequest request = new ConnectBasketRequest(qrCode, userId);

        ApiClient.getApiService().connectToBasket(token, request).enqueue(new Callback<ConnectBasketResponse>() {
            @Override
            public void onResponse(Call<ConnectBasketResponse> call, Response<ConnectBasketResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int sessionId = response.body().getSessionId();
                    sessionManager.saveSessionId(sessionId);

                    Toast.makeText(CartActivity.this, "Корзина успешно подключена!", Toast.LENGTH_SHORT).show();

                    openActiveCartFragment();
                } else {
                    Toast.makeText(CartActivity.this, "Ошибка подключения: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ConnectBasketResponse> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void openActiveCartFragment() {
        emptyCartContainer.setVisibility(View.GONE);

        Fragment activeCartFragment = new ActiveCartFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.active_cart_fragment_container, activeCartFragment);

        transaction.commit();
    }
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
