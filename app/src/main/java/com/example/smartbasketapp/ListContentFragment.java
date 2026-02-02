package com.example.smartbasketapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListContentFragment extends Fragment implements ListContentAdapter.OnListContentInteractionListener {
    private int listId;
    private String listName;
    private RecyclerView recyclerView;
    private ListContentAdapter adapter;
    private List<ListContentItem> contentList = new ArrayList<>();
    private SessionManager sessionManager;
    private TextView totalPriceTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listId = getArguments().getInt("LIST_ID");
            listName = getArguments().getString("LIST_NAME");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.listContentRecyclerView);
        totalPriceTextView = view.findViewById(R.id.totalPriceTextView);

        toolbar.setTitle(listName);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        adapter = new ListContentAdapter(getContext(), contentList, this);
        recyclerView.setAdapter(adapter);

        loadContent();
    }

    private void loadContent() {
        String token = "Bearer " + sessionManager.getAuthToken();
        ApiClient.getApiService().getListContents(token, listId).enqueue(new Callback<ListContentResponse>() {
            @Override
            public void onResponse(Call<ListContentResponse> call, Response<ListContentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ListContentItem> loadedContent = response.body().getContent();
                    if (loadedContent != null) {
                        contentList.clear();
                        contentList.addAll(loadedContent);
                        adapter.notifyDataSetChanged();
                        calculateTotal();
                    }
                }
            }

            @Override
            public void onFailure(Call<ListContentResponse> call, Throwable t) { /* ... */ }
        });
    }

    private void calculateTotal() {
        double total = 0;
        for (ListContentItem item : contentList) {
            total += item.getPrice() * item.getRequiredQuantity();
        }
        totalPriceTextView.setText(String.format(Locale.getDefault(), "%.2f BYN", total));
    }

    @Override
    public void onDeleteClick(ListContentItem item, int position) {
        String token = "Bearer " + sessionManager.getAuthToken();
        RemoveFromListContentRequest request = new RemoveFromListContentRequest(listId, item.getId());

        ApiClient.getApiService().removeFromListContents(token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    contentList.remove(position);
                    adapter.notifyItemRemoved(position);
                    calculateTotal();
                } else {
                    Toast.makeText(getContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) { /* ... */ }
        });
    }

    @Override
    public void onCheckedChange(ListContentItem item, boolean isChecked) {
        String token = "Bearer " + sessionManager.getAuthToken();
        UpdateCheckedRequest request = new UpdateCheckedRequest(listId, item.getId(), isChecked);

        ApiClient.getApiService().updateListItemChecked(token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadContent();
                } else {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Ошибка обновления", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }
}