package com.example.smartbasketapp;
import static java.security.AccessController.getContext;

import com.example.smartbasketapp.Category;

import android.content.Context;
import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener, ProductAdapter.OnProductInteractionListener
{
    private RecyclerView categoryRecyclerView;
    private RecyclerView productRecyclerView;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList = new ArrayList<>();
    private ProductAdapter productAdapter;
    private EditText searchEditText;
    private List<Product> productList = new ArrayList<>();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_search);

        sessionManager = new SessionManager(this);

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        productRecyclerView = findViewById(R.id.productRecyclerView);

        categoryAdapter = new CategoryAdapter(this, categoryList, this);
        BottomNavigationView navView = findViewById(R.id.bottomNavigation);
        categoryRecyclerView.setAdapter(categoryAdapter);

        productAdapter = new ProductAdapter(this, productList, this);
        productRecyclerView.setAdapter(productAdapter);

        navView.setSelectedItemId(R.id.navigation_search);
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(SearchActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_cart) {
                startActivity(new Intent(SearchActivity.this, CartActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                 startActivity(new Intent(SearchActivity.this, ProfileActivity.class));
                 overridePendingTransition(0, 0);
                 finish();
                return true;
            } else if (itemId == R.id.navigation_search) {
                return true;
            }

            return false;
        });
        searchEditText = findViewById(R.id.searchEditText);
        ImageButton searchGoButton = findViewById(R.id.searchButton);

        searchGoButton.setOnClickListener(v -> performSearch());

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        loadCategories();
    }
    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Введите поисковый запрос", Toast.LENGTH_SHORT).show();
            return;
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        loadProducts(ApiClient.getApiService().searchProducts(query));
        ApiClient.getApiService().searchProducts(query).enqueue(new Callback<ProductSearchResponse>() {
            @Override
            public void onResponse(Call<ProductSearchResponse> call, Response<ProductSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // TODO: Реализуйте вложенный запрос для проверки избранного, как в MainActivity
                    productList.clear();
                    productList.addAll(response.body().getProducts());
                    productAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(SearchActivity.this, "Ничего не найдено", Toast.LENGTH_SHORT).show();
                    productList.clear();
                    productAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<ProductSearchResponse> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadCategories() {
        ApiClient.getApiService().getCategories().enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body().getCategories();
                    categoryList.clear();
                    categoryList.addAll(categories);
                    categoryAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onCategoryClick(Category category) {
        ApiClient.getApiService().getProductsByCategory(category.getId()).enqueue(new Callback<ProductSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductSearchResponse> call, @NonNull Response<ProductSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body().getProducts());
                    productAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<ProductSearchResponse> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        loadProducts(ApiClient.getApiService().getProductsByCategory(category.getId()));
    }
    @Override
    public void onFavoriteClick(Product product, int position) {

        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();

        Callback<Void> callback = new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    product.setFavorite(!product.isFavorite());
                    productAdapter.notifyItemChanged(position);
                } else {
                    Toast.makeText(SearchActivity.this, "Ошибка. Код: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(SearchActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if (product.isFavorite()) {
            RemoveFromFavoritesRequest request = new RemoveFromFavoritesRequest(userId, product.getId());
            ApiClient.getApiService().removeFromFavorites(token, request).enqueue(callback);
        } else {
            AddToFavoritesRequest request = new AddToFavoritesRequest(userId, product.getId());
            ApiClient.getApiService().addToFavorites(token, request).enqueue(callback);
        }
    }

    @Override
    public void onAddToListClick(Product product) {
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();
        ApiClient.getApiService().getShoppingLists(userId, token).enqueue(new Callback<ShoppingListsResponse>() {
            @Override
            public void onResponse(Call<ShoppingListsResponse> call, Response<ShoppingListsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showShoppingListsDialog(response.body().getLists(), product);
                } else {
                    Log.e("SearchActivity", "Ошибка загрузки списков покупок. Код: " + response.code());
                    Toast.makeText(SearchActivity.this, "Не удалось загрузить списки", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ShoppingListsResponse> call, Throwable t) {
                Log.e("SearchActivity", "Ошибка сети (списки): " + t.getMessage());
            }
        });
    }
    private void showShoppingListsDialog(List<ShoppingList> lists, Product product) {
        List<String> listNames = new ArrayList<>();
        for (ShoppingList list : lists) {
            listNames.add(list.getName());
        }
        listNames.add("Создать новый список...");

        new AlertDialog.Builder(this)
                .setTitle("Добавить \"" + product.getName() + "\" в список")
                .setItems(listNames.toArray(new String[0]), (dialog, which) -> {
                    if (which == listNames.size() - 1) {
                        showCreateListDialog(product);
                    } else {
                        int listId = lists.get(which).getId();
                        addProductToList(listId, product.getId());
                    }
                })
                .show();
    }
    private void showCreateListDialog(Product product) {
        final EditText input = new EditText(this);
        input.setHint("Название списка");

        new AlertDialog.Builder(this)
                .setTitle("Создать новый список")
                .setView(input)
                .setPositiveButton("Создать", (dialog, which) -> {
                    String listName = input.getText().toString().trim();
                    if (!listName.isEmpty()) {
                        createListAndAddProduct(listName, product.getId());
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void createListAndAddProduct(String listName, int productId) {
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();
        //   CreateListRequest request = new CreateListRequest(userId, listName);
        ApiClient.getApiService().createShoppingList(token, new CreateListRequest(userId, listName)).enqueue(new Callback<ShoppingList>() {
            @Override
            public void onResponse(Call<ShoppingList> call, Response<ShoppingList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int newListId = response.body().getId();
                    addProductToList(newListId, productId);
                }
            }
            @Override
            public void onFailure(Call<ShoppingList> call, Throwable t) { /* ... */ }
        });
    }

    private void addProductToList(int listId, int productId) {
        String token = "Bearer " + sessionManager.getAuthToken();
        AddToListRequest request = new AddToListRequest(listId, productId, 1);
        ApiClient.getApiService().addToListContents(token, request).enqueue(new Callback<Void>(){
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(SearchActivity.this, "Товар добавлен в список", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { /* ... */ }
        });
    }
    @Override
    public void onProductClick(Product product) {
        // TODO: переход на детальный экран продукта
        Toast.makeText(this, "Нажали на товар: " + product.getName(), Toast.LENGTH_SHORT).show();
    }
    private void loadProducts(Call<ProductSearchResponse> apiCall) {
        String token = "Bearer " + sessionManager.getAuthToken();

        apiCall.enqueue(new Callback<ProductSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductSearchResponse> call, @NonNull Response<ProductSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> loadedProducts = response.body().getProducts();
                    ApiClient.getApiService().getFavorites(sessionManager.getUserId(), token).enqueue(new Callback<FavoritesResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<FavoritesResponse> call, @NonNull Response<FavoritesResponse> favResponse) {
                            if (favResponse.isSuccessful() && favResponse.body() != null) {
                                Set<Integer> favoriteIds = new HashSet<>();
                                for (Product p : favResponse.body().getFavorites()) {
                                    favoriteIds.add(p.getId());
                                }

                                for (Product p : loadedProducts) {
                                    if (favoriteIds.contains(p.getId())) {
                                        p.setFavorite(true);
                                    }
                                }
                            }
                            productList.clear();
                            productList.addAll(loadedProducts);
                            productAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(@NonNull Call<FavoritesResponse> call, @NonNull Throwable t) {
                            productList.clear();
                            productList.addAll(loadedProducts);
                            productAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    Toast.makeText(SearchActivity.this, "Ничего не найдено", Toast.LENGTH_SHORT).show();
                    productList.clear();
                    productAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductSearchResponse> call, @NonNull Throwable t) {
                Toast.makeText(SearchActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}