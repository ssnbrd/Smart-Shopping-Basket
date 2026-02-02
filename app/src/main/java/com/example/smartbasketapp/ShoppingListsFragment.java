package com.example.smartbasketapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShoppingListsFragment extends Fragment implements ShoppingListAdapter.OnListInteractionListener {

    private List<ShoppingList> shoppingLists;
    private ShoppingListAdapter adapter;
    private SessionManager sessionManager;

    public ShoppingListsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shopping_lists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        RecyclerView recyclerView = view.findViewById(R.id.shoppingListsRecyclerView);
        FloatingActionButton fab = view.findViewById(R.id.fabAddList);

        shoppingLists = new ArrayList<>();
        adapter = new ShoppingListAdapter(shoppingLists, this);
        recyclerView.setAdapter(adapter);

        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        fab.setOnClickListener(v -> showAddOrRenameDialog(null, -1));

        loadShoppingLists();
    }

    private void loadShoppingLists() {
        Log.d("ShoppingLists", "1. Начинаю загрузку списков...");
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Log.e("ShoppingLists", "Ошибка: userId не найден. Загрузка отменена.");
            return;
        }

        ApiClient.getApiService().getShoppingLists(userId, token).enqueue(new Callback<ShoppingListsResponse>() {
            @Override
            public void onResponse(Call<ShoppingListsResponse> call, Response<ShoppingListsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ShoppingLists", "2. Ответ от сервера УСПЕШНЫЙ.");
                    List<ShoppingList> loadedLists = response.body().getLists();

                    if (loadedLists != null) {
                        Log.d("ShoppingLists", "3. Получено списков: " + loadedLists.size());
                        shoppingLists.clear();
                        shoppingLists.addAll(loadedLists);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("ShoppingLists", "ОШИБКА: Сервер вернул null вместо списка.");
                    }
                } else {
                    Log.e("ShoppingLists", "ОШИБКА: Ответ от сервера НЕ успешный. Код: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ShoppingListsResponse> call, Throwable t) {
                Log.e("ShoppingLists", "КРИТИЧЕСКАЯ ОШИБКА СЕТИ: " + t.getMessage());
            }
        });
    }

    @Override
    public void onRenameClick(int position) {
        showAddOrRenameDialog(shoppingLists.get(position), position);
    }

    @Override
    public void onDeleteClick(int position) {
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        ShoppingList listToDelete = shoppingLists.get(position);

        new AlertDialog.Builder(requireContext())
                .setTitle("Удалить список")
                .setMessage("Вы уверены, что хотите удалить список '" + listToDelete.getName() + "'?")
                .setPositiveButton("Да", (dialog, which) -> {
                    ApiClient.getApiService().deleteShoppingList(token, listToDelete.getId(), new DeleteListRequest(userId))
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        shoppingLists.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        Toast.makeText(getContext(), "Список удален", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Ошибка удаления. Код: " + response.code(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    @Override
    public void onListClick(int position) {
        ShoppingList clickedList = shoppingLists.get(position);

        ListContentFragment contentFragment = new ListContentFragment();

        Bundle args = new Bundle();
        args.putInt("LIST_ID", clickedList.getId());
        args.putString("LIST_NAME", clickedList.getName());
        contentFragment.setArguments(args);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, contentFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showAddOrRenameDialog(ShoppingList existingList, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        final EditText input = new EditText(requireActivity());
        input.setHint("Название списка");

        if (existingList != null) {
            builder.setTitle("Переименовать список");
            input.setText(existingList.getName());
        } else {
            builder.setTitle("Создать новый список");
        }

        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String listName = input.getText().toString().trim();
            if (!listName.isEmpty()) {
                if (existingList != null) {
                    renameListOnServer(existingList, listName, position);
                } else {
                    createListOnServer(listName);
                }
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    private void createListOnServer(String name) {
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        CreateListRequest request = new CreateListRequest(userId, name);

        ApiClient.getApiService().createShoppingList(token, request).enqueue(new Callback<ShoppingList>() {
            @Override
            public void onResponse(Call<ShoppingList> call, Response<ShoppingList> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Список '" + name + "' создан", Toast.LENGTH_SHORT).show();
                    loadShoppingLists();
                } else {
                    Toast.makeText(getContext(), "Ошибка создания. Код: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ShoppingList> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renameListOnServer(ShoppingList list, String newName, int position) {
        String token = "Bearer " + sessionManager.getAuthToken();
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        RenameListRequest request = new RenameListRequest(userId, newName);

        ApiClient.getApiService().renameShoppingList(token, list.getId(), request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    list.setName(newName);
                    adapter.notifyItemChanged(position);
                } else {
                    Toast.makeText(getContext(), "Ошибка переименования. Код: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}