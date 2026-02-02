package com.example.smartbasketapp;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import java.util.Collections;

public class MainActivity extends AppCompatActivity
        implements ProductAdapter.OnProductInteractionListener {
    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private EditText searchEditText;
    private List<Product> productList = new ArrayList<>();
    private ViewPager2 promoViewPager;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private SessionManager sessionManager;
    private static final String TAG = "MainActivity_List";
    private final ActivityResultLauncher<ScanOptions> qrCodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_LONG).show();
                } else {
                    String scannedData = result.getContents();
                    Toast.makeText(this, "Отсканировано: " + scannedData, Toast.LENGTH_LONG).show();
                    connectToBasket(scannedData);
                }
            });
    private void connectToBasket(String qrCode) {
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();

        if (userId == -1) { return; }

        ConnectBasketRequest request = new ConnectBasketRequest(qrCode, userId);

        ApiClient.getApiService().connectToBasket(token, request).enqueue(new Callback<ConnectBasketResponse>() {
            @Override
            public void onResponse(Call<ConnectBasketResponse> call, Response<ConnectBasketResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int sessionId = response.body().getSessionId();
                    sessionManager.saveSessionId(sessionId);

                    Intent intent = new Intent(MainActivity.this, CartActivity.class);
                    intent.putExtra("SHOW_ACTIVE_CART", true);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ConnectBasketResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sessionManager = new SessionManager(this);

        productRecyclerView = findViewById(R.id.productRecyclerView);
        productAdapter = new ProductAdapter(this, productList, this);
        productRecyclerView.setAdapter(productAdapter);

        searchEditText = findViewById(R.id.searchEditText);
        ImageButton searchButton = findViewById(R.id.searchButton);

        loadProducts("");
        LinearLayout scanButton = findViewById(R.id.scanQrButton);

        scanButton.setOnClickListener(v -> {
            startQrScanner();
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.navigation_home) {
                return true;
            } else if (id == R.id.navigation_search) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.navigation_cart) {
                startActivity(new Intent(MainActivity.this, CartActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.navigation_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
        searchButton.setOnClickListener(v -> {
            performSearch();
        });
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
        loadProducts("");
        setupPromoSlider();
    }
    private void loadProducts(String query) {
        Log.d("MainActivity", "1. Начало загрузки продуктов...");
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            //Toast.makeText(getContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.getApiService().searchProducts(query).enqueue(new Callback<ProductSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductSearchResponse> call, @NonNull Response<ProductSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("MainActivity", "2. Продукты успешно загружены: " + response.body().getProducts().size() + " шт.");
                    List<Product> loadedProducts = response.body().getProducts();
                    if (loadedProducts != null) {
                        Collections.shuffle(loadedProducts);
                    }
                    ApiClient.getApiService().getFavorites(userId, token).enqueue(new Callback<FavoritesResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<FavoritesResponse> call, @NonNull Response<FavoritesResponse> favResponse) {
                            if (favResponse.isSuccessful() && favResponse.body() != null) {
                                Log.d("MainActivity", "3. Избранное успешно загружено.");
                                List<Product> favoriteProducts = favResponse.body().getFavorites();
                                Set<Integer> favoriteIds = new HashSet<>();
                                for (Product p : favoriteProducts) {
                                    favoriteIds.add(p.getId());
                                }

                                for (Product p : loadedProducts) {
                                    if (favoriteIds.contains(p.getId())) {
                                        p.setFavorite(true);
                                    }
                                }
                            }
                            Log.d("MainActivity", "4. Обновление UI...");
                            productList.clear();
                            productList.addAll(loadedProducts);
                            productAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(@NonNull Call<FavoritesResponse> call, @NonNull Throwable t) {
                            Log.e("MainActivity", "Ошибка загрузки избранного: " + t.getMessage());
                            productList.clear();
                            productList.addAll(loadedProducts);
                            productAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    Log.e("MainActivity", "Ошибка загрузки продуктов. Код: " + response.code());
                    Toast.makeText(MainActivity.this, "Ошибка загрузки продуктов", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductSearchResponse> call, @NonNull Throwable t) {
                Log.e("MainActivity", "КРИТИЧЕСКАЯ ОШИБКА СЕТИ (продукты): " + t.getMessage());
                Toast.makeText(MainActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void startQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(true);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Наведите камеру на QR-код на корзине");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true);
        qrCodeLauncher.launch(options);
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
            int currentItem = promoViewPager.getCurrentItem();
            int nextItem = currentItem + 1;
            if (nextItem >= sliderAdapter.getItemCount()) {
                nextItem = 0;
            }
            promoViewPager.setCurrentItem(nextItem, true);
        };

        sliderHandler.postDelayed(sliderRunnable, 3000);

        promoViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sliderRunnable != null) {
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
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
                    Toast.makeText(MainActivity.this, "Ошибка. Код: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        Log.d("AddToList", "1. Нажата кнопка 'Добавить в список'.");
        String token = "Bearer " + sessionManager.getAuthToken();

        int userId = sessionManager.getUserId();

        if (userId == -1) {  return; }
        Log.d(TAG, "2. Отправляю запрос на получение списков...");
        ApiClient.getApiService().getShoppingLists(userId, token).enqueue(new Callback<ShoppingListsResponse>() {
            @Override
            public void onResponse(@NonNull Call<ShoppingListsResponse> call, @NonNull Response<ShoppingListsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ShoppingList> loadedLists = response.body().getLists();
                    if (loadedLists != null) {
                        Log.d(TAG, "3. Списки успешно загружены. Количество: " + loadedLists.size());
                        showShoppingListsDialog(loadedLists, product);
                    } else {
                        Log.e(TAG, "ОШИБКА: Сервер вернул null вместо списка. Показываем диалог с пустым списком.");
                        showShoppingListsDialog(new ArrayList<>(), product);
                    }
                } else {
                    Log.e(TAG, "ОШИБКА: Не удалось загрузить списки. Код ответа: " + response.code());
                    Toast.makeText(MainActivity.this, "Не удалось загрузить списки", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ShoppingListsResponse> call, Throwable t) {
                Log.e("MainActivity", "Ошибка сети (списки): " + t.getMessage());
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
        Log.d(TAG, "4. Пользователь создал новый список '" + listName + "'. Отправляю запрос на сервер...");
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();
        if (userId == -1) return;
        CreateListRequest request = new CreateListRequest(userId, listName);

        ApiClient.getApiService().createShoppingList(token, request).enqueue(new Callback<ShoppingList>() {
            @Override
            public void onResponse(Call<ShoppingList> call, Response<ShoppingList> response) {
                if (response.isSuccessful() && response.body() != null) {

                    int newListId = response.body().getId();
                    Log.d(TAG, "5. Список успешно создан. ID нового списка: " + newListId);
                    addProductToList(newListId, productId);

                    Toast.makeText(MainActivity.this, "Список '" + listName + "' создан", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "Ошибка создания списка", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ShoppingList> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addProductToList(int listId, int productId) {
        Log.d(TAG, "6. Добавляю продукт ID " + productId + " в список ID " + listId + "...");
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();
        if (userId == -1) return;
        AddToListRequest request = new AddToListRequest(listId, productId, 1);
        ApiClient.getApiService().addToListContents(token, request).enqueue(new Callback<Void>(){
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                    Log.d(TAG, "7. ПРОДУКТ УСПЕШНО ДОБАВЛЕН!");
                    Toast.makeText(MainActivity.this, "Товар добавлен в список", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "ОШИБКА добавления продукта в список. Код: " + response.code());
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
    private void performSearch() {
        String query = searchEditText.getText().toString().trim();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

        loadProducts(query);
    }
}

