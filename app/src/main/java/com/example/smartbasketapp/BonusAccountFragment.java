package com.example.smartbasketapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import java.util.Locale;

public class BonusAccountFragment extends Fragment {

    private TextView bonusAmountTextView;

    public BonusAccountFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bonus_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        bonusAmountTextView = view.findViewById(R.id.bonusAmountTextView);

        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        if (getArguments() != null) {
            double bonuses = getArguments().getDouble("USER_BONUSES", 0.0);

            bonusAmountTextView.setText(String.format(Locale.getDefault(), "%.1f", bonuses));
        }
    }
}