package com.example.smartbasketapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {

    private static final String PREFS = "SmartBasketTheme";
    private static final String KEY_MODE = "nightMode";

    private static final int[] MODES = {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES
    };

    private ThemeManager() {}

    public static void apply(Context context) {
        AppCompatDelegate.setDefaultNightMode(getSavedMode(context));
    }

    public static void showThemeDialog(Context context) {
        String[] names = {
                context.getString(R.string.theme_system),
                context.getString(R.string.theme_light),
                context.getString(R.string.theme_dark)
        };
        int current = getSavedMode(context);
        int checked = 0;
        for (int i = 0; i < MODES.length; i++) {
            if (MODES[i] == current) checked = i;
        }

        new AlertDialog.Builder(context)
                .setTitle(R.string.theme_title)
                .setSingleChoiceItems(names, checked, (dialog, which) -> {
                    dialog.dismiss();
                    setMode(context, MODES[which]);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static void setMode(Context context, int mode) {
        prefs(context).edit().putInt(KEY_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    private static int getSavedMode(Context context) {
        return prefs(context).getInt(KEY_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
