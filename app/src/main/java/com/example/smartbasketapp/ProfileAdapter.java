package com.example.smartbasketapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    public interface OnOptionClickListener {
        void onOptionClick(String optionTitle);
    }

    private final List<ProfileOption> options;
    private final OnOptionClickListener listener;
    private final Context context;

    public ProfileAdapter(Context context, List<ProfileOption> options, OnOptionClickListener listener) {
        this.context = context;
        this.options = options;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_option, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        ProfileOption option = options.get(position);

        holder.title.setText(option.getTitle());

        if (option.isHighlighted()) {
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.link_green));
            holder.arrow.setColorFilter(ContextCompat.getColor(context, R.color.link_green));
        } else {
            holder.title.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            holder.arrow.setColorFilter(ContextCompat.getColor(context, android.R.color.black));
        }

        holder.itemView.setOnClickListener(v -> {
            listener.onOptionClick(option.getTitle());
        });
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final ImageView arrow;
        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.optionTitle);
            arrow = itemView.findViewById(R.id.optionArrow);
        }
    }
}