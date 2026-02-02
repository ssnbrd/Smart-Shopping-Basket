package com.example.smartbasketapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesFragment extends Fragment implements ProductAdapter.OnProductInteractionListener {
    private RecyclerView favoritesRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> favoriteProductList;
    private SessionManager sessionManager;

    public FavoritesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        favoritesRecyclerView = view.findViewById(R.id.favoritesRecyclerView);
        sessionManager = new SessionManager(requireContext());

        favoriteProductList = new ArrayList<>();

        productAdapter = new ProductAdapter(getContext(), favoriteProductList, this);
        favoritesRecyclerView.setAdapter(productAdapter);

        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

    }
    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }
    private void loadFavorites() {
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(getContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.getApiService().getFavorites(userId, token).enqueue(new Callback<FavoritesResponse>() {
            @Override
            public void onResponse(@NonNull Call<FavoritesResponse> call, @NonNull Response<FavoritesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoriteProductList.clear();
                    favoriteProductList.addAll(response.body().getFavorites());

                    for (Product p : favoriteProductList) {
                        p.setFavorite(true);
                    }

                    productAdapter.notifyDataSetChanged();
                } else {
                    String errorMsg = "Ошибка загрузки избранного. Код: " + response.code();
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getContext(), "Ошибка загрузки избранного", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FavoritesResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFavoriteClick(Product product, int position) {

        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();

        RemoveFromFavoritesRequest request = new RemoveFromFavoritesRequest(userId, product.getId());


        ApiClient.getApiService().removeFromFavorites(token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    favoriteProductList.remove(position);
                    productAdapter.notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Удалено из избранного", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Ошибка удаления. Код: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onAddToListClick(Product product) {
        Toast.makeText(getContext(), "Добавить в список: " + product.getName(), Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onProductClick(Product product) {
        // TODO: Переход на детальный экран продукта
        Toast.makeText(getContext(), "Нажали на: " + product.getName(), Toast.LENGTH_SHORT).show();
    }
}