package com.example.smartbasketapp;
import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity implements ProfileAdapter.OnOptionClickListener {
    private View profileContentContainer;
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileContentContainer = findViewById(R.id.profile_content_container);
        RecyclerView recyclerView = findViewById(R.id.profileOptionsRecyclerView);
        ImageButton logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Выход из аккаунта")
                    .setMessage("Вы уверены, что хотите выйти?")
                    .setPositiveButton("Да", (dialog, which) -> logoutUser())
                    .setNegativeButton("Нет", null)
                    .show();
        });
        userNameTextView = findViewById(R.id.userName);
        userEmailTextView = findViewById(R.id.userEmail);
        sessionManager = new SessionManager(this);

        List<ProfileOption> profileOptions = new ArrayList<>();

            profileOptions.add(new ProfileOption("История покупок", false));
            profileOptions.add(new ProfileOption("Избранное", false));
            profileOptions.add(new ProfileOption("Список покупок", false));
            profileOptions.add(new ProfileOption("Способы оплаты", false));
            profileOptions.add(new ProfileOption("Бонусный счёт", true));
            profileOptions.add(new ProfileOption("О приложении", false));

        ProfileAdapter adapter = new ProfileAdapter(this, profileOptions, this);
        recyclerView.setAdapter(adapter);

        BottomNavigationView navView = findViewById(R.id.bottomNavigation);
        navView.setSelectedItemId(R.id.navigation_profile);

        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_search) {
                startActivity(new Intent(ProfileActivity.this, SearchActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_cart) {
                startActivity(new Intent(ProfileActivity.this, CartActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                return true;
            }
            return false;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {

                    getSupportFragmentManager().popBackStack();

                } else {
                    if (isEnabled()) {
                        setEnabled(false);
                        ProfileActivity.this.getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            }
        });

        loadUserProfile();
    }
    @Override
    public void onOptionClick(String optionTitle) {
        Fragment fragment = null;
        if ("Способы оплаты".equals(optionTitle)) {
            PaymentMethodsBottomSheet bottomSheet = new PaymentMethodsBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "PaymentMethodsBottomSheet");
        }
        else if ("Бонусный счёт".equals(optionTitle)) {
            calculateAndShowBonuses();
            return;
        }
        else {
        switch (optionTitle) {
            case "История покупок":
                fragment = new HistoryFragment();
                break;
            case "Избранное":
                fragment = new FavoritesFragment();
                break;
            case "Список покупок":
                fragment = new ShoppingListsFragment();
                break;
            case "О приложении":
                fragment = new AboutAppFragment();
                break;
        }
}
        if (fragment != null) {
            openFragment(fragment);
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
        );

        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }
    private void loadUserProfile() {
        String token = "Bearer " + sessionManager.getAuthToken();
        SessionManager sessionManager = new SessionManager(this);
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Ошибка: ID пользователя не найден", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.getApiService().getUserProfile(userId).enqueue(new Callback<UserProfileResponse>()
                {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    String name = response.body().getUser().getName();
                    String surname = response.body().getUser().getSurname();
                    String email = response.body().getUser().getEmail();
                    String fullName = name + " " + surname;

                    userNameTextView.setText(fullName);
                    userEmailTextView.setText(email);
                }
                else {
                    Toast.makeText(ProfileActivity.this, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void calculateAndShowBonuses() {
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        ApiClient.getApiService().getTransactionHistory(userId, token).enqueue(new Callback<TransactionResponse>() {
            @Override
            public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                double bonuses = 0.0;
                if (response.isSuccessful() && response.body() != null) {
                    List<Transaction> transactions = response.body().getTransactions();
                    if (transactions != null) {
                        bonuses = transactions.size() * 0.1;
                    }
                }

                BonusAccountFragment bonusFragment = new BonusAccountFragment();
                Bundle args = new Bundle();
                args.putDouble("USER_BONUSES", bonuses);
                bonusFragment.setArguments(args);

                openFragment(bonusFragment);
            }

            @Override
            public void onFailure(Call<TransactionResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Не удалось загрузить бонусы", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void logoutUser() {
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.clear();

        Intent intent = new Intent(ProfileActivity.this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}