package com.example.smartbasketapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    public interface OnCartInteractionListener {
        void onDeleteClick(CartProduct product, int position);
    }
    private final Context context;
    private final List<CartProduct> productList;
    private final OnCartInteractionListener listener;
    public CartAdapter(Context context, List<CartProduct> productList, OnCartInteractionListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartProduct product = productList.get(position);

        holder.productName.setText(product.getName());
        holder.productPrice.setText(String.format(Locale.getDefault(), "%.2f BYN", product.getPrice()));
        holder.quantityTextView.setText(String.format(Locale.getDefault(), "%d шт.", product.getScannedQuantity()));

        Glide.with(context).load(product.getImageUrl()).into(holder.productImage);

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(product, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, quantityTextView, productDescription;
        ImageButton favoriteButton, addToListButton, deleteButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            // productDescription = itemView.findViewById(R.id.productDescription);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            addToListButton = itemView.findViewById(R.id.addToListButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}