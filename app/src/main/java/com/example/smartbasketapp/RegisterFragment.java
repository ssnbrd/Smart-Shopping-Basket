package com.example.smartbasketapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    public RegisterFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText nameEditText = view.findViewById(R.id.nameEditText);
        EditText surnameEditText = view.findViewById(R.id.surnameEditText);
        EditText emailEditText = view.findViewById(R.id.emailEditText);
        EditText passwordEditText = view.findViewById(R.id.passwordEditText);
        Button registerButton = view.findViewById(R.id.registerButton);

        Button goToLoginButton = view.findViewById(R.id.goToLoginButton);

        registerButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String surname = surnameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            RegisterRequest request = new RegisterRequest(name, surname, email, password);
            registerUser(request);
        });

        goToLoginButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
    }

    private void registerUser(RegisterRequest request) {
        ApiClient.getApiService().registerUser(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // `isSuccessful` означает код ответа 2xx (например, 200 OK, 201 Created)
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Регистрация успешна! Теперь вы можете войти.", Toast.LENGTH_LONG).show();

                    getParentFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), "Ошибка регистрации: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}