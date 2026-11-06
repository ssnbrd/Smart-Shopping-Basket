package com.example.smartbasketapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class BonusAccountFragment extends Fragment {

    public static final String ARG_RESULT = "BONUS_RESULT";

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
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        BonusCalculator.Result result = null;
        if (getArguments() != null) {
            result = (BonusCalculator.Result) getArguments().getSerializable(ARG_RESULT);
        }
        if (result == null) result = BonusCalculator.calculate(null);

        ((TextView) view.findViewById(R.id.bonusAmountTextView))
                .setText(String.format(Locale.getDefault(), "%.2f", result.points));

        ((TextView) view.findViewById(R.id.bonusLevelTextView))
                .setText(getString(R.string.bonus_level_format, levelName(result.level)));
        ((TextView) view.findViewById(R.id.bonusRateTextView))
                .setText(getString(R.string.bonus_rate_format, result.level.percent));
        ((TextView) view.findViewById(R.id.bonusSpentTextView))
                .setText(getString(R.string.bonus_spent_format, result.totalSpent));

        ProgressBar progress = view.findViewById(R.id.bonusProgress);
        progress.setProgress(result.progressPercent());
        TextView nextLevel = view.findViewById(R.id.bonusNextLevelTextView);
        BonusCalculator.Level next = result.level.next();
        if (next == null) {
            nextLevel.setText(R.string.bonus_max_level);
        } else {
            nextLevel.setText(getString(R.string.bonus_next_level_format,
                    levelName(next), result.amountToNextLevel()));
        }

        BonusCalculator.Level silver = BonusCalculator.Level.SILVER;
        BonusCalculator.Level gold = BonusCalculator.Level.GOLD;
        ((TextView) view.findViewById(R.id.bonusRulesTextView)).setText(getString(
                R.string.bonus_rules_format,
                BonusCalculator.Level.BASIC.percent,
                silver.threshold, silver.percent,
                gold.threshold, gold.percent));
    }

    private String levelName(BonusCalculator.Level level) {
        switch (level) {
            case SILVER:
                return getString(R.string.bonus_level_silver);
            case GOLD:
                return getString(R.string.bonus_level_gold);
            default:
                return getString(R.string.bonus_level_basic);
        }
    }
}
