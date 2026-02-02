package com.example.smartbasketapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginFragment extends Fragment {

     private SessionManager sessionManager;
     private EditText emailEditText;
     private EditText passwordEditText;
     public LoginFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        Button loginButton = view.findViewById(R.id.loginButton);
        Button goToRegisterButton = view.findViewById(R.id.goToRegisterButton);

        loginButton.setOnClickListener(v -> attemptLogin());

        goToRegisterButton.setOnClickListener(v -> {

            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_auth, new RegisterFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Введите email и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(email, password);
        loginUser(request);
    }

    private void loginUser(LoginRequest request) {
        ApiClient.getApiService().loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    int userId = loginResponse.getUser().getIdUser();

                    String fakeToken = "user_logged_in_" + userId;
                    Log.d("AuthCheck", "Сервер не вернул токен. Генерируем фейковый: " + fakeToken);

                    sessionManager.saveAuthToken(fakeToken);
                    sessionManager.saveUserId(userId);
                    Log.d("AuthCheck", "Фейковый токен сохранен. Проверяем: " + new SessionManager(requireContext()).getAuthToken());

                    Toast.makeText(getContext(), "Вход выполнен успешно!", Toast.LENGTH_SHORT).show();

                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    }

                } else {
                    Toast.makeText(getContext(), "Ошибка входа: неверный email или пароль", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}