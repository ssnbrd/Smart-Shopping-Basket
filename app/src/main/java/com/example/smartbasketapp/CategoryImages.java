package com.example.smartbasketapp;

import android.content.Context;

import androidx.annotation.DrawableRes;

import java.util.Locale;

public final class CategoryImages {

    private CategoryImages() {}

    private static final Object[][] RULES = {
            {R.drawable.cat_frozen, new String[]{"заморож", "мороз", "замарож", "марож", "frozen", "ice cream"}},
            {R.drawable.cat_fish, new String[]{"рыб", "морепродукт", "fish", "seafood"}},
            {R.drawable.cat_meat, new String[]{"мяс", "колбас", "птиц", "meat", "sausage", "poultry"}},
            {R.drawable.cat_dairy, new String[]{"молоч", "малоч", "сыр", "яйц", "яйк", "йогурт", "кефир", "сметан", "dairy", "milk", "cheese", "egg"}},
            {R.drawable.cat_bakery, new String[]{"хлеб", "выпеч", "булоч", "bread", "bakery", "pastry"}},
            {R.drawable.cat_fruits, new String[]{"овощ", "фрукт", "ягод", "зелен", "авощ", "садавін", "fruit", "vegetable", "berry"}},
            {R.drawable.cat_drinks, new String[]{"напит", "напо", "сок", "сокі", "чай", "гарбат", "кофе", "кава", "вод", "drink", "beverage", "juice", "water", "coffee", "tea"}},
            {R.drawable.cat_sweets, new String[]{"слад", "салодк", "конфет", "цукер", "шоколад", "десерт", "печень", "кондитер", "снек", "sweet", "candy", "chocolate", "snack", "dessert", "cookie"}},
            {R.drawable.cat_grocery, new String[]{"бакал", "круп", "макарон", "консерв", "кансерв", "мука", "grocery", "pasta", "cereal", "canned", "flour"}},
            {R.drawable.cat_household, new String[]{"хим", "быт", "побыт", "гигиен", "уборк", "уборка", "household", "cleaning", "hygiene", "детерг"}},
    };

    @DrawableRes
    public static int forCategory(Context context, Category category) {
        int custom = context.getResources().getIdentifier(
                "category_" + category.getId(), "drawable", context.getPackageName());
        if (custom != 0) return custom;

        String name = category.getName() == null ? "" : category.getName().toLowerCase(Locale.ROOT);
        for (Object[] rule : RULES) {
            for (String keyword : (String[]) rule[1]) {
                if (name.contains(keyword)) return (Integer) rule[0];
            }
        }
        return R.drawable.cat_default;
    }
}
