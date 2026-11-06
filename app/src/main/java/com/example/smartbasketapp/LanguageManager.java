package com.example.smartbasketapp;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public final class LanguageManager {

    private static final String[] TAGS = {"", "ru", "en", "be"};

    private LanguageManager() {}

    public static void showLanguageDialog(Context context) {
        String[] names = {
                context.getString(R.string.language_system),
                context.getString(R.string.language_ru),
                context.getString(R.string.language_en),
                context.getString(R.string.language_be)
        };

        new AlertDialog.Builder(context)
                .setTitle(R.string.language_title)
                .setSingleChoiceItems(names, getCurrentIndex(), (dialog, which) -> {
                    dialog.dismiss();
                    setLanguage(TAGS[which]);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static void setLanguage(String tag) {
        AppCompatDelegate.setApplicationLocales(tag.isEmpty()
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(tag));
    }

    private static int getCurrentIndex() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (locales.isEmpty() || locales.get(0) == null) return 0;
        String language = locales.get(0).getLanguage();
        for (int i = 1; i < TAGS.length; i++) {
            if (TAGS[i].equals(language)) return i;
        }
        return 0;
    }
}
