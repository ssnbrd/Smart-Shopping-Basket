package com.example.smartbasketapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class TransactionDetailsAdapter extends RecyclerView.Adapter<TransactionDetailsAdapter.ViewHolder> {

    private final List<TransactionDetail> detailList;

    public TransactionDetailsAdapter(List<TransactionDetail> detailList) {
        this.detailList = detailList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionDetail detail = detailList.get(position);

        holder.productName.setText(detail.getName());

        String quantityDetails = String.format(Locale.getDefault(), "%d шт. x %.2f BYN",
                detail.getQuantity(), detail.getPrice());
        holder.quantity.setText(quantityDetails);

        double itemTotal = detail.getQuantity() * detail.getPrice();
        holder.price.setText(String.format(Locale.getDefault(), "%.2f BYN", itemTotal));
    }

    @Override
    public int getItemCount() {
        return detailList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, quantity, price;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productNameTextView);
            quantity = itemView.findViewById(R.id.quantityTextView);
            price = itemView.findViewById(R.id.priceTextView);
        }
    }
}