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
            Toast.makeText(getContext(), getString(R.string.error_user_not_authorized), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), getString(R.string.error_code_format, response.code()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FavoritesResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_network_format, t.getMessage()), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.delete_error_format, response.code()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_network_format, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onAddToListClick(Product product) {
        ProductActions.showAddToList(requireContext(), product);
    }
    @Override
    public void onProductClick(Product product) {
        product.setFavorite(true);
        startActivity(ProductDetailActivity.createIntent(requireContext(), product));
    }
}