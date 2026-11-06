package com.example.smartbasketapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity
        implements CategoryAdapter.OnCategoryClickListener, ProductAdapter.OnProductInteractionListener {

    private RecyclerView categoryRecyclerView;
    private RecyclerView productRecyclerView;
    private CategoryAdapter categoryAdapter;
    public static final String EXTRA_QUERY = "EXTRA_QUERY";

    private final List<Category> categoryList = new ArrayList<>();
    private ProductAdapter productAdapter;
    private final List<Product> productList = new ArrayList<>();
    private EditText searchEditText;
    private TextView screenTitle;
    private TextView emptyTextView;
    private ImageButton backToCategoriesButton;

    private int requestCounter = 0;

    private OnBackPressedCallback backCallback;

    private final ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (result.getResultCode() == RESULT_OK && data != null && productAdapter != null) {
                    productAdapter.setFavoriteById(
                            data.getIntExtra(ProductDetailActivity.RESULT_PRODUCT_ID, -1),
                            data.getBooleanExtra(ProductDetailActivity.RESULT_FAVORITE, false));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        productRecyclerView = findViewById(R.id.productRecyclerView);
        screenTitle = findViewById(R.id.screenTitle);
        emptyTextView = findViewById(R.id.emptyTextView);
        backToCategoriesButton = findViewById(R.id.backToCategoriesButton);
        searchEditText = findViewById(R.id.searchEditText);
        ImageButton searchGoButton = findViewById(R.id.searchButton);

        categoryAdapter = new CategoryAdapter(this, categoryList, this);
        categoryRecyclerView.setAdapter(categoryAdapter);
        productAdapter = new ProductAdapter(this, productList, this);
        productRecyclerView.setAdapter(productAdapter);

        setupBottomNavigation();

        backToCategoriesButton.setOnClickListener(v -> showCategories());
        backCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                showCategories();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backCallback);

        searchGoButton.setOnClickListener(v -> performSearch());
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        loadCategories();

        String initialQuery = getIntent().getStringExtra(EXTRA_QUERY);
        if (initialQuery != null && !initialQuery.trim().isEmpty()) {
            searchEditText.setText(initialQuery);
            performSearch();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView navView = findViewById(R.id.bottomNavigation);
        navView.setSelectedItemId(R.id.navigation_search);
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Class<?> target = null;
            if (itemId == R.id.navigation_home) {
                target = MainActivity.class;
            } else if (itemId == R.id.navigation_cart) {
                target = CartActivity.class;
            } else if (itemId == R.id.navigation_profile) {
                target = ProfileActivity.class;
            } else if (itemId == R.id.navigation_search) {
                return true;
            }
            if (target == null) return false;
            startActivity(new Intent(SearchActivity.this, target));
            overridePendingTransition(0, 0);
            finish();
            return true;
        });
    }

    private void showCategories() {
        requestCounter++;
        backCallback.setEnabled(false);
        backToCategoriesButton.setVisibility(View.GONE);
        screenTitle.setText(R.string.category);
        productRecyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
        categoryRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showProductsState(String title) {
        backCallback.setEnabled(true);
        backToCategoriesButton.setVisibility(View.VISIBLE);
        screenTitle.setText(title);
        categoryRecyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
        productRecyclerView.setVisibility(View.VISIBLE);
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

        if (query.isEmpty()) {
            showCategories();
            return;
        }
        showProductsState(query);
        loadProducts(ApiClient.getApiService().searchProducts(query));
    }

    private void loadCategories() {
        ApiClient.getApiService().getCategories().enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<CategoryResponse> call, @NonNull Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCategories() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body().getCategories());
                    categoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CategoryResponse> call, @NonNull Throwable t) {
                Toast.makeText(SearchActivity.this,
                        getString(R.string.error_network_format, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCategoryClick(Category category) {
        showProductsState(category.getName());
        loadProducts(ApiClient.getApiService().getProductsByCategory(category.getId()));
    }

    private void loadProducts(Call<ProductSearchResponse> apiCall) {
        final int myRequest = ++requestCounter;
        productList.clear();
        productAdapter.notifyDataSetChanged();

        apiCall.enqueue(new Callback<ProductSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductSearchResponse> call, @NonNull Response<ProductSearchResponse> response) {
                if (myRequest != requestCounter) return;
                List<Product> loaded = response.isSuccessful() && response.body() != null
                        ? response.body().getProducts() : null;
                if (loaded == null || loaded.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                    return;
                }
                ProductActions.markFavorites(SearchActivity.this, loaded, () -> {
                    if (myRequest != requestCounter) return;
                    productList.clear();
                    productList.addAll(loaded);
                    productAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(@NonNull Call<ProductSearchResponse> call, @NonNull Throwable t) {
                if (myRequest != requestCounter) return;
                Toast.makeText(SearchActivity.this,
                        getString(R.string.error_network_format, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFavoriteClick(Product product, int position) {
        ProductActions.toggleFavorite(this, product, isFavorite -> productAdapter.notifyItemChanged(position));
    }

    @Override
    public void onAddToListClick(Product product) {
        ProductActions.showAddToList(this, product);
    }

    @Override
    public void onProductClick(Product product) {
        detailLauncher.launch(ProductDetailActivity.createIntent(this, product));
    }
}
