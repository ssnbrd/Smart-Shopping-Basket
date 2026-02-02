package com.example.smartbasketapp;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.Locale;

public class ListContentAdapter extends RecyclerView.Adapter<ListContentAdapter.ViewHolder> {

    public interface OnListContentInteractionListener {
        void onDeleteClick(ListContentItem item, int position);
        void onCheckedChange(ListContentItem item, boolean isChecked);
        // Сюда можно добавить onFavoriteClick, onQuantityChange и т.д.
    }

    private final Context context;
    private final List<ListContentItem> itemList;
    private final OnListContentInteractionListener listener;

    public ListContentAdapter(Context context, List<ListContentItem> itemList, OnListContentInteractionListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListContentItem item = itemList.get(position);

        holder.productName.setText(item.getName());
        holder.productPrice.setText(String.format(Locale.getDefault(), "%.2f BYN", item.getPrice()));
        holder.quantityTextView.setText(String.format(Locale.getDefault(), "%d шт.", item.getRequiredQuantity()));

        Glide.with(context).load(item.getImageUrl()).into(holder.productImage);

        holder.checkbox.setOnCheckedChangeListener(null);
        holder.checkbox.setChecked(item.isChecked());

        if (item.isChecked()) {
            holder.productName.setPaintFlags(holder.productName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setAlpha(0.5f);
        } else {
            holder.productName.setPaintFlags(holder.productName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.itemView.setAlpha(1.0f);
        }

        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(item, position));
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.onCheckedChange(item, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, quantityTextView;
        ImageButton deleteButton;
        CheckBox checkbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }
}