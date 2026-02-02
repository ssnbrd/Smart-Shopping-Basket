package com.example.smartbasketapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionDetailsFragment extends Fragment {

    private int transactionId;
    private RecyclerView recyclerView;
    private TransactionDetailsAdapter adapter;
    private List<TransactionDetail> detailList = new ArrayList<>();
    private SessionManager sessionManager;
    private TextView totalAmountTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transactionId = getArguments().getInt("TRANSACTION_ID");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_details, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.detailsRecyclerView);
        totalAmountTextView = view.findViewById(R.id.totalAmountTextView);

        toolbar.setTitle("Чек №" + transactionId);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        adapter = new TransactionDetailsAdapter(detailList);
        recyclerView.setAdapter(adapter);

        loadDetails();
    }

    private void loadDetails() {
        Log.d("TransactionDetails", "1. Начинаю загрузку деталей для транзакции ID: " + transactionId);
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        ApiClient.getApiService().getTransactionDetails(transactionId, userId, token).enqueue(new Callback<TransactionDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<TransactionDetailsResponse> call, @NonNull Response<TransactionDetailsResponse> response) {

                if (response.isSuccessful()) {
                    Log.d("TransactionDetails", "2. Ответ от сервера УСПЕШНЫЙ. Код: " + response.code());

                    TransactionDetailsResponse body = response.body();

                    if (body != null && body.getDetails() != null) {
                        Log.d("TransactionDetails", "3. Тело ответа (body) НЕ null. Найдено позиций в чеке: " + body.getDetails().size());

                        detailList.clear();
                        detailList.addAll(body.getDetails());
                        adapter.notifyDataSetChanged();
                        calculateTotal();

                        Log.d("TransactionDetails", "4. Адаптер обновлен. Всего элементов: " + adapter.getItemCount());
                    } else {
                        Log.e("TransactionDetails", "ОШИБКА: Тело ответа (body) или список 'details' ПУСТЫЕ (null)!");
                    }

                } else {
                    Log.e("TransactionDetails", "ОШИБКА: Ответ от сервера НЕ успешный. Код: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TransactionDetailsResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateTotal() {
        double total = 0;
        for (TransactionDetail detail : detailList) {
            total += detail.getPrice() * detail.getQuantity();
        }
        totalAmountTextView.setText(String.format(Locale.getDefault(), "%.2f BYN", total));
    }
}