package com.example.smartbasketapp;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    private final List<Transaction> transactionList;
    private final OnTransactionClickListener listener;
    private final Context context;

    public HistoryAdapter(Context context, List<Transaction> transactionList, OnTransactionClickListener listener) {
        this.context = context;
        this.transactionList = transactionList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.transactionId.setText("Покупка №" + transaction.getId());
        holder.transactionAmount.setText(String.format(Locale.getDefault(), "%.2f BYN", transaction.getTotalAmount()));

        holder.transactionDate.setText(transaction.getDateTime());
        holder.itemView.setOnClickListener(v -> listener.onTransactionClick(transaction));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView transactionId, transactionDate, transactionAmount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionId = itemView.findViewById(R.id.transactionIdTextView);
            transactionDate = itemView.findViewById(R.id.transactionDateTextView);
            transactionAmount = itemView.findViewById(R.id.transactionAmountTextView);
        }
    }
}