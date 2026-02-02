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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment implements HistoryAdapter.OnTransactionClickListener {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.historyRecyclerView);

        adapter = new HistoryAdapter(getContext(), transactionList, this);
        recyclerView.setAdapter(adapter);

        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        loadHistory();
    }

    private void loadHistory() {
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        ApiClient.getApiService().getTransactionHistory(userId, token).enqueue(new Callback<TransactionResponse>() {
            @Override
            public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Transaction> loaded = response.body().getTransactions();
                    if(loaded != null) {
                        transactionList.clear();
                        transactionList.addAll(loaded);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onFailure(Call<TransactionResponse> call, Throwable t) { /* ... */ }
        });
    }

    @Override
    public void onTransactionClick(Transaction transaction) {

        TransactionDetailsFragment detailsFragment = new TransactionDetailsFragment();

        Bundle args = new Bundle();
        args.putInt("TRANSACTION_ID", transaction.getId());
        detailsFragment.setArguments(args);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit();

        Toast.makeText(getContext(), "Нажата покупка №" + transaction.getId(), Toast.LENGTH_SHORT).show();
    }}