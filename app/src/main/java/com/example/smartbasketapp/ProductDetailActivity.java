package com.example.smartbasketapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetail";

    public static final String EXTRA_PRODUCT = "EXTRA_PRODUCT";
    public static final String EXTRA_FAVORITE = "EXTRA_FAVORITE";
    public static final String RESULT_PRODUCT_ID = "RESULT_PRODUCT_ID";
    public static final String RESULT_FAVORITE = "RESULT_FAVORITE";

    private Product product;
    private ImageButton favoriteButton;
    private TextView descriptionView;

    public static Intent createIntent(Context context, Product product) {
        Intent intent = new Intent(context, ProductDetailActivity.class);
        intent.putExtra(EXTRA_PRODUCT, product);
        intent.putExtra(EXTRA_FAVORITE, product.isFavorite());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        product = (Product) getIntent().getSerializableExtra(EXTRA_PRODUCT);
        if (product == null) {
            Toast.makeText(this, R.string.product_load_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        product.setFavorite(getIntent().getBooleanExtra(EXTRA_FAVORITE, false));

        ImageButton backButton = findViewById(R.id.backButton);
        ImageView image = findViewById(R.id.productImage);
        TextView name = findViewById(R.id.productName);
        TextView price = findViewById(R.id.productPrice);
        descriptionView = findViewById(R.id.productDescription);
        favoriteButton = findViewById(R.id.favoriteButton);
        ImageButton listButton = findViewById(R.id.addToListButton);

        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        final int baseMargin = ((android.view.ViewGroup.MarginLayoutParams) backButton.getLayoutParams()).topMargin;
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            android.view.ViewGroup.MarginLayoutParams lp =
                    (android.view.ViewGroup.MarginLayoutParams) backButton.getLayoutParams();
            lp.topMargin = baseMargin + bars.top;
            backButton.setLayoutParams(lp);
            return windowInsets;
        });

        name.setText(product.getName());
        price.setText(getString(R.string.price_format, product.getPrice()));

        showDescription(product.getDescription());
        if (isBlank(product.getDescription())) {
            loadDescriptionFromServer();
        }

        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(image);

        updateFavoriteIcon();
        favoriteButton.setOnClickListener(v ->
                ProductActions.toggleFavorite(this, product, isFavorite -> {
                    updateFavoriteIcon();
                    publishResult();
                    Toast.makeText(this,
                            isFavorite ? R.string.favorite_added : R.string.favorite_removed,
                            Toast.LENGTH_SHORT).show();
                }));
        listButton.setOnClickListener(v -> ProductActions.showAddToList(this, product));
    }

    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private void showDescription(String text) {
        descriptionView.setText(isBlank(text)
                ? getString(R.string.product_description_empty)
                : text.trim());
    }

    private void loadDescriptionFromServer() {
        ApiClient.getApiService().getProductDetails(product.getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        if (isFinishing() || isDestroyed()) return;
                        String body = null;
                        try {
                            ResponseBody responseBody = response.isSuccessful()
                                    ? response.body() : response.errorBody();
                            if (responseBody != null) body = responseBody.string();
                        } catch (IOException e) {
                            Log.w(TAG, "Cannot read response", e);
                        }
                        if (!response.isSuccessful()) {
                            Log.w(TAG, "GET /api/product/" + product.getId()
                                    + " -> HTTP " + response.code() + ": " + body);
                            return;
                        }
                        String text = ProductDescriptionParser.extract(body);
                        if (text == null) {
                            Log.w(TAG, "No description in response of /api/product/"
                                    + product.getId() + ": " + body);
                            return;
                        }
                        showDescription(text);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Log.w(TAG, "GET /api/product/" + product.getId() + " failed", t);
                    }
                });
    }

    private void updateFavoriteIcon() {
        favoriteButton.setImageResource(product.isFavorite()
                ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        favoriteButton.setContentDescription(getString(product.isFavorite()
                ? R.string.remove_from_favorites : R.string.add_in_favorites));
    }

    private void publishResult() {
        Intent result = new Intent();
        result.putExtra(RESULT_PRODUCT_ID, product.getId());
        result.putExtra(RESULT_FAVORITE, product.isFavorite());
        setResult(RESULT_OK, result);
    }
}
