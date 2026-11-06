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
import java.util.Collections;
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
        logoutButton.setOnClickListener(v -> confirmLogout());
        userNameTextView = findViewById(R.id.userName);
        userEmailTextView = findViewById(R.id.userEmail);
        sessionManager = new SessionManager(this);

        List<ProfileOption> profileOptions = new ArrayList<>();

        profileOptions.add(new ProfileOption(ProfileOption.HISTORY, R.string.option_history, false));
        profileOptions.add(new ProfileOption(ProfileOption.FAVORITES, R.string.favorite, false));
        profileOptions.add(new ProfileOption(ProfileOption.SHOPPING_LISTS, R.string.shopping_lists, false));
        profileOptions.add(new ProfileOption(ProfileOption.PAYMENT, R.string.option_payment, false));
        profileOptions.add(new ProfileOption(ProfileOption.BONUS, R.string.option_bonus, true));
        profileOptions.add(new ProfileOption(ProfileOption.LANGUAGE, R.string.option_language, false));
        profileOptions.add(new ProfileOption(ProfileOption.THEME, R.string.option_theme, false));
        profileOptions.add(new ProfileOption(ProfileOption.ABOUT, R.string.option_about, false));

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
    public void onOptionClick(int optionId) {
        switch (optionId) {
            case ProfileOption.PAYMENT:
                new PaymentMethodsBottomSheet().show(getSupportFragmentManager(), "PaymentMethodsBottomSheet");
                break;
            case ProfileOption.BONUS:
                calculateAndShowBonuses();
                break;
            case ProfileOption.LANGUAGE:
                LanguageManager.showLanguageDialog(this);
                break;
            case ProfileOption.THEME:
                ThemeManager.showThemeDialog(this);
                break;
            case ProfileOption.HISTORY:
                openFragment(new HistoryFragment());
                break;
            case ProfileOption.FAVORITES:
                openFragment(new FavoritesFragment());
                break;
            case ProfileOption.SHOPPING_LISTS:
                openFragment(new ShoppingListsFragment());
                break;
            case ProfileOption.ABOUT:
                openFragment(new AboutAppFragment());
                break;
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> logoutUser())
                .setNegativeButton(R.string.no, null)
                .show();
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
            Toast.makeText(this, R.string.profile_user_id_missing, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ProfileActivity.this, R.string.profile_load_error, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, getString(R.string.error_network_format, t.getMessage()), Toast.LENGTH_SHORT).show();
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
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ProfileActivity.this, R.string.bonus_load_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                List<Transaction> transactions = response.body().getTransactions();
                if (transactions == null) transactions = new ArrayList<>();

                List<Transaction> sorted = new ArrayList<>(transactions);
                Collections.sort(sorted, (a, b) -> Integer.compare(a.getId(), b.getId()));
                List<Double> totals = new ArrayList<>();
                for (Transaction t : sorted) {
                    totals.add(t.getTotalAmount());
                }

                BonusAccountFragment bonusFragment = new BonusAccountFragment();
                Bundle args = new Bundle();
                args.putSerializable(BonusAccountFragment.ARG_RESULT, BonusCalculator.calculate(totals));
                bonusFragment.setArguments(args);
                openFragment(bonusFragment);
            }

            @Override
            public void onFailure(Call<TransactionResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, R.string.bonus_load_error, Toast.LENGTH_SHORT).show();
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