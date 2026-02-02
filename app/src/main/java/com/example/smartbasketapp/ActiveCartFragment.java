package com.example.smartbasketapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActiveCartFragment extends Fragment implements CartAdapter.OnCartInteractionListener{
    private double currentTotal = 0.0;
    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<CartProduct> cartProductList = new ArrayList<>();
    private SessionManager sessionManager;

    private Handler updateHandler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;
    private TextView totalPriceTextView;
    private static final int UPDATE_INTERVAL = 5000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_active_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cartRecyclerView = view.findViewById(R.id.cartRecyclerView);
        sessionManager = new SessionManager(requireContext());

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        Button payButton = toolbar.findViewById(R.id.payButton);

        payButton = view.findViewById(R.id.payButton);
        payButton.setOnClickListener(v -> finishSession());

        cartAdapter = new CartAdapter(getContext(), cartProductList, this);
        cartRecyclerView.setAdapter(cartAdapter);

        totalPriceTextView = view.findViewById(R.id.totalPriceTextView);

        updateRunnable = () -> {
            loadCartContents();
            updateHandler.postDelayed(updateRunnable, UPDATE_INTERVAL);
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        updateHandler.post(updateRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        updateHandler.removeCallbacks(updateRunnable);
    }

    private void loadCartContents() {
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();

        if (userId == -1) { /* Пользователь не авторизован */ return; }

        ApiClient.getApiService().getBasketContents(userId, token).enqueue(new Callback<CartContentResponse>() {
            @Override
            public void onResponse(Call<CartContentResponse> call, Response<CartContentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CartProduct> newProducts = response.body().getContent();
                    cartProductList.clear();
                    cartProductList.addAll(newProducts);
                    cartAdapter.notifyDataSetChanged();

                    updateTotalPrice(newProducts);
                }
            }
            @Override
            public void onFailure(Call<CartContentResponse> call, Throwable t) {

            }
        });
    }
    private void updateTotalPrice(List<CartProduct> products) {
        double total = 0.0;
        for (CartProduct product : products) {
            total += product.getPrice() * product.getScannedQuantity();
        }
        currentTotal = total;
        totalPriceTextView.setText(String.format(Locale.getDefault(), "%.2f BYN", total));
    }
    private void finishSession() {
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();

        FinishSessionRequest request = new FinishSessionRequest(userId, currentTotal);

        ApiClient.getApiService().finishSession(token, request).enqueue(new Callback<FinishSessionResponse>() {
            @Override
            public void onResponse(Call<FinishSessionResponse> call, Response<FinishSessionResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Оплата прошла успешно!", Toast.LENGTH_LONG).show();
                    sessionManager.clearSession();

                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
            @Override
            public void onFailure(Call<FinishSessionResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onDeleteClick(CartProduct product, int position) {
        String token = "Bearer " + sessionManager.getAuthToken();
        RemoveFromBasketRequest request = new RemoveFromBasketRequest(product.getId());

        ApiClient.getApiService().removeFromBasket(token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadCartContents();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { /* ... */ }
        });
    }
}