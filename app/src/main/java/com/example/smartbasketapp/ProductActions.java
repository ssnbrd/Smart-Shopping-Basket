package com.example.smartbasketapp;

import android.app.Activity;
import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class ProductActions {

    public interface FavoriteCallback {
        void onChanged(boolean isFavorite);
    }

    private ProductActions() {}

    public static void markFavorites(@NonNull Context context, @NonNull List<Product> products,
                                     @NonNull Runnable done) {
        SessionManager session = new SessionManager(context);
        int userId = session.getUserId();
        if (userId == -1) {
            done.run();
            return;
        }
        String token = "Bearer " + session.getAuthToken();
        ApiClient.getApiService().getFavorites(userId, token).enqueue(new Callback<FavoritesResponse>() {
            @Override
            public void onResponse(@NonNull Call<FavoritesResponse> call, @NonNull Response<FavoritesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getFavorites() != null) {
                    java.util.Set<Integer> ids = new java.util.HashSet<>();
                    for (Product p : response.body().getFavorites()) {
                        ids.add(p.getId());
                    }
                    for (Product p : products) {
                        p.setFavorite(ids.contains(p.getId()));
                    }
                }
                done.run();
            }

            @Override
            public void onFailure(@NonNull Call<FavoritesResponse> call, @NonNull Throwable t) {
                done.run();
            }
        });
    }

    public static void toggleFavorite(@NonNull Context context, @NonNull Product product,
                                      FavoriteCallback callback) {
        SessionManager session = new SessionManager(context);
        int userId = session.getUserId();
        if (userId == -1) {
            Toast.makeText(context, R.string.error_user_not_authorized, Toast.LENGTH_SHORT).show();
            return;
        }
        String token = "Bearer " + session.getAuthToken();
        final boolean wasFavorite = product.isFavorite();

        Callback<Void> responseHandler = new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    product.setFavorite(!wasFavorite);
                    if (callback != null) callback.onChanged(product.isFavorite());
                } else {
                    Toast.makeText(context,
                            context.getString(R.string.error_code_format, response.code()),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(context,
                        context.getString(R.string.error_network_format, t.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        };

        if (wasFavorite) {
            ApiClient.getApiService()
                    .removeFromFavorites(token, new RemoveFromFavoritesRequest(userId, product.getId()))
                    .enqueue(responseHandler);
        } else {
            ApiClient.getApiService()
                    .addToFavorites(token, new AddToFavoritesRequest(userId, product.getId()))
                    .enqueue(responseHandler);
        }
    }

    public static void showAddToList(@NonNull Context context, @NonNull Product product) {
        SessionManager session = new SessionManager(context);
        int userId = session.getUserId();
        if (userId == -1) {
            Toast.makeText(context, R.string.error_user_not_authorized, Toast.LENGTH_SHORT).show();
            return;
        }
        String token = "Bearer " + session.getAuthToken();

        ApiClient.getApiService().getShoppingLists(userId, token).enqueue(new Callback<ShoppingListsResponse>() {
            @Override
            public void onResponse(@NonNull Call<ShoppingListsResponse> call, @NonNull Response<ShoppingListsResponse> response) {
                if (!canShowDialog(context)) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<ShoppingList> lists = response.body().getLists();
                    showListsDialog(context, lists != null ? lists : new ArrayList<>(), product);
                } else {
                    Toast.makeText(context, R.string.list_load_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ShoppingListsResponse> call, @NonNull Throwable t) {
                Toast.makeText(context,
                        context.getString(R.string.error_network_format, t.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void showListsDialog(Context context, List<ShoppingList> lists, Product product) {
        List<String> names = new ArrayList<>();
        for (ShoppingList list : lists) {
            names.add(list.getName());
        }
        names.add(context.getString(R.string.create_new_list_item));

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.add_to_list_title_format, product.getName()))
                .setItems(names.toArray(new String[0]), (dialog, which) -> {
                    if (which == names.size() - 1) {
                        showCreateListDialog(context, product);
                    } else {
                        addProductToList(context, lists.get(which).getId(), product.getId());
                    }
                })
                .show();
    }

    private static void showCreateListDialog(Context context, Product product) {
        final EditText input = new EditText(context);
        input.setHint(R.string.list_name_hint);

        new AlertDialog.Builder(context)
                .setTitle(R.string.create_new_list)
                .setView(input)
                .setPositiveButton(R.string.create, (dialog, which) -> {
                    String listName = input.getText().toString().trim();
                    if (!listName.isEmpty()) {
                        createListAndAddProduct(context, listName, product.getId());
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private static void createListAndAddProduct(Context context, String listName, int productId) {
        SessionManager session = new SessionManager(context);
        int userId = session.getUserId();
        if (userId == -1) return;
        String token = "Bearer " + session.getAuthToken();

        ApiClient.getApiService().createShoppingList(token, new CreateListRequest(userId, listName))
                .enqueue(new Callback<ShoppingList>() {
                    @Override
                    public void onResponse(@NonNull Call<ShoppingList> call, @NonNull Response<ShoppingList> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(context,
                                    context.getString(R.string.list_created_format, listName),
                                    Toast.LENGTH_SHORT).show();
                            addProductToList(context, response.body().getId(), productId);
                        } else {
                            Toast.makeText(context, R.string.list_create_error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ShoppingList> call, @NonNull Throwable t) {
                        Toast.makeText(context,
                                context.getString(R.string.error_network_format, t.getMessage()),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static void addProductToList(Context context, int listId, int productId) {
        String token = "Bearer " + new SessionManager(context).getAuthToken();

        ApiClient.getApiService().addToListContents(token, new AddToListRequest(listId, productId, 1))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        Toast.makeText(context,
                                response.isSuccessful() ? R.string.product_added_to_list
                                                        : R.string.product_add_to_list_error,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(context,
                                context.getString(R.string.error_network_format, t.getMessage()),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static boolean canShowDialog(Context context) {
        if (context instanceof Activity) {
            Activity a = (Activity) context;
            return !a.isFinishing() && !a.isDestroyed();
        }
        return true;
    }
}
