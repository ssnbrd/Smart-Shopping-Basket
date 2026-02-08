package com.example.smartbasketapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.util.Patterns;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private EditText nameEditText, surnameEditText, emailEditText, passwordEditText;
    private Button registerButton, goToLoginButton;
    public RegisterFragment() {    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameEditText = view.findViewById(R.id.nameEditText);
        surnameEditText = view.findViewById(R.id.surnameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);

        registerButton = view.findViewById(R.id.registerButton);
        goToLoginButton = view.findViewById(R.id.goToLoginButton);

        emailInputLayout = view.findViewById(R.id.emailInputLayout);
        passwordInputLayout = view.findViewById(R.id.passwordInputLayout);

        registerButton.setOnClickListener(v -> attemptRegistration());
        emailEditText.addTextChangedListener(new EmailValidationTextWatcher());

        addTextWatcher(emailEditText, emailInputLayout);
        addTextWatcher(passwordEditText, passwordInputLayout);
        goToLoginButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
    }
            private void attemptRegistration() {
                if (!validateForm()) {
                    return;
                }

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
            }
            private boolean validateForm() {
                emailInputLayout.setError(null);

                String email = emailEditText.getText().toString().trim();

                if (email.isEmpty()) {
                    emailInputLayout.setError("Поле не может быть пустым");
                    return false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInputLayout.setError("Некорректный формат email");
                    return false;
                }

                String password = passwordEditText.getText().toString().trim();
                if (password.length() < 8) {
                    passwordInputLayout.setError("Пароль должен быть не менее 8 символов");
                    return false;
                } else if (!password.matches(".*\\d.*") || !password.matches(".*[a-zA-Z].*")) {
                    passwordInputLayout.setError("Пароль должен содержать буквы и цифры");
                    return false;
                }

                return true;
            }
            private class EmailValidationTextWatcher implements TextWatcher {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String email = s.toString().trim();
                    if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailInputLayout.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            }
            private void addTextWatcher(EditText editText, TextInputLayout inputLayout) {
            editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    inputLayout.setError(null);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
            private void registerUser(RegisterRequest request) {
            ApiClient.getApiService().registerUser(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
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