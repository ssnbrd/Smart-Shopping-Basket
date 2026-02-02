package com.example.smartbasketapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ListViewHolder> {

    public interface OnListInteractionListener {
        void onRenameClick(int position);
        void onDeleteClick(int position);
        void onListClick(int position);
    }

    private final List<ShoppingList> shoppingLists;
    private final OnListInteractionListener listener;

    public ShoppingListAdapter(List<ShoppingList> shoppingLists, OnListInteractionListener listener) {
        this.shoppingLists = shoppingLists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping_list, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        ShoppingList currentList = shoppingLists.get(position);
        holder.listName.setText(currentList.getName());

        holder.itemView.setOnClickListener(v -> listener.onListClick(position));

        holder.optionsMenu.setOnClickListener(v -> showPopupMenu(v.getContext(), v, position));
    }

    private void showPopupMenu(Context context, View view, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.shopping_list_options, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_rename) {
                listener.onRenameClick(position);
                return true;
            } else if (itemId == R.id.action_delete) {
                listener.onDeleteClick(position);
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return shoppingLists.size();
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        final TextView listName;
        final ImageButton optionsMenu;

        ListViewHolder(@NonNull View itemView) {
            super(itemView);
            listName = itemView.findViewById(R.id.listNameTextView);
            optionsMenu = itemView.findViewById(R.id.optionsMenuButton);
        }
    }
}