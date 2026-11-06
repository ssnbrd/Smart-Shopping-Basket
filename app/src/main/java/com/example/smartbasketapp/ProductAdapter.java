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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    public interface OnProductInteractionListener {
        void onFavoriteClick(Product product, int position);
        void onAddToListClick(Product product);
        void onProductClick(Product product);
    }
    private final Context context;
    private final List<Product> productList;
    private final OnProductInteractionListener listener;
    public ProductAdapter(Context context, List<Product> productList, OnProductInteractionListener  listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productName.setText(product.getName());
        holder.productPrice.setText(context.getString(R.string.price_format, product.getPrice()));

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.productImage);

        if (product.isFavorite()) {
            holder.addToFavoritesButton.setImageResource(R.drawable.ic_favorite);
        } else {
            holder.addToFavoritesButton.setImageResource(R.drawable.ic_favorite_border);
        }
        holder.addToFavoritesButton.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onFavoriteClick(product, pos);
        });
        holder.addToShoppingListButton.setOnClickListener(v -> listener.onAddToListClick(product));
        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void setFavoriteById(int productId, boolean favorite) {
        for (int i = 0; i < productList.size(); i++) {
            Product p = productList.get(i);
            if (p.getId() == productId && p.isFavorite() != favorite) {
                p.setFavorite(favorite);
                notifyItemChanged(i);
            }
        }
    }
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice;
        ImageButton addToFavoritesButton, addToShoppingListButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            addToFavoritesButton = itemView.findViewById(R.id.addToFavoritesButton);
            addToShoppingListButton = itemView.findViewById(R.id.addToShoppingListButton);
        }
    }
}
