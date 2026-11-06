package com.example.smartbasketapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements ProductAdapter.OnProductInteractionListener {

    public static final String EXTRA_START_SCAN = "EXTRA_START_SCAN";

    private static final long SLIDER_DELAY_MS = 3000;

    private ProductAdapter productAdapter;
    private EditText searchEditText;
    private final List<Product> productList = new ArrayList<>();
    private ViewPager2 promoViewPager;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<ScanOptions> qrCodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(this, R.string.scan_cancelled, Toast.LENGTH_LONG).show();
                } else {
                    connectToBasket(result.getContents());
                }
            });

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
        setContentView(R.layout.activity_main);
        sessionManager = new SessionManager(this);

        RecyclerView productRecyclerView = findViewById(R.id.productRecyclerView);
        productAdapter = new ProductAdapter(this, productList, this);
        productRecyclerView.setAdapter(productAdapter);

        searchEditText = findViewById(R.id.searchEditText);
        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> performSearch());
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        findViewById(R.id.cartWidget).setOnClickListener(v -> onCartWidgetClick());

        setupBottomNavigation();
        setupPromoSlider();
        loadRecommendations();

        if (savedInstanceState == null && getIntent().getBooleanExtra(EXTRA_START_SCAN, false)) {
            getIntent().removeExtra(EXTRA_START_SCAN);
            findViewById(R.id.main).post(this::onCartWidgetClick);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Class<?> target = null;
            if (id == R.id.navigation_home) {
                return true;
            } else if (id == R.id.navigation_search) {
                target = SearchActivity.class;
            } else if (id == R.id.navigation_cart) {
                target = CartActivity.class;
            } else if (id == R.id.navigation_profile) {
                target = ProfileActivity.class;
            }
            if (target == null) return false;
            startActivity(new Intent(MainActivity.this, target));
            overridePendingTransition(0, 0);
            finish();
            return true;
        });
    }

    private void onCartWidgetClick() {
        if (sessionManager.getSessionId() != -1) {
            Intent intent = new Intent(this, CartActivity.class);
            intent.putExtra("SHOW_ACTIVE_CART", true);
            startActivity(intent);
            finish();
        } else {
            startQrScanner();
        }
    }

    private void startQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(true);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt(getString(R.string.scan_prompt));
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true);
        qrCodeLauncher.launch(options);
    }

    private void connectToBasket(String qrCode) {
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, R.string.error_user_not_authorized, Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.getApiService().connectToBasket(token, new ConnectBasketRequest(qrCode, userId))
                .enqueue(new Callback<ConnectBasketResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ConnectBasketResponse> call, @NonNull Response<ConnectBasketResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            sessionManager.saveSessionId(response.body().getSessionId());

                            Intent intent = new Intent(MainActivity.this, CartActivity.class);
                            intent.putExtra("SHOW_ACTIVE_CART", true);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.basket_connect_error_format, response.message()),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ConnectBasketResponse> call, @NonNull Throwable t) {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.error_network_format, t.getMessage()),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadRecommendations() {
        ApiClient.getApiService().searchProducts("").enqueue(new Callback<ProductSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductSearchResponse> call, @NonNull Response<ProductSearchResponse> response) {
                List<Product> loaded = response.isSuccessful() && response.body() != null
                        ? response.body().getProducts() : null;
                if (loaded == null) {
                    Toast.makeText(MainActivity.this, R.string.products_load_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                Collections.shuffle(loaded);
                ProductActions.markFavorites(MainActivity.this, loaded, () -> {
                    productList.clear();
                    productList.addAll(loaded);
                    productAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(@NonNull Call<ProductSearchResponse> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.error_network_format, t.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_QUERY, query);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void setupPromoSlider() {
        promoViewPager = findViewById(R.id.promoViewPager);

        List<Integer> imageList = new ArrayList<>();
        imageList.add(R.drawable.promo_image_1);
        imageList.add(R.drawable.promo_image_2);
        imageList.add(R.drawable.promo_image_3);

        PromoSliderAdapter sliderAdapter = new PromoSliderAdapter(imageList);
        promoViewPager.setAdapter(sliderAdapter);

        sliderRunnable = () -> {
            int next = promoViewPager.getCurrentItem() + 1;
            if (next >= sliderAdapter.getItemCount()) next = 0;
            promoViewPager.setCurrentItem(next, true);
        };

        promoViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                restartSliderTimer();
            }
        });
    }

    private void restartSliderTimer() {
        sliderHandler.removeCallbacks(sliderRunnable);
        sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        restartSliderTimer();
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
