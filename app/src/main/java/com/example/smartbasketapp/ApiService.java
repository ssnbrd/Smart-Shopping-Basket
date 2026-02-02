package com.example.smartbasketapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("/api/register")
    Call<Void> registerUser(@Body RegisterRequest registerRequest);
    @POST("/api/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);
    @GET("/api/products/search")
    Call<ProductSearchResponse> searchProducts(@Query("q") String query);
    @GET("/api/user/{user_id}")
    Call<UserProfileResponse> getUserProfile(@Path("user_id") int userId);
    @GET("/api/categories")
    Call<CategoryResponse> getCategories();
    @POST("/api/favorites")
    Call<Void> addToFavorites(@Header("Authorization") String token, @Body AddToFavoritesRequest request);
    @HTTP(method = "DELETE", path = "/api/lists/{list_id}", hasBody = true)
    Call<Void> deleteShoppingList(@Header("Authorization") String token, @Path("list_id") int listId, @Body DeleteListRequest request);
    @GET("/api/products/category/{id}")
    Call<ProductSearchResponse> getProductsByCategory(@Path("id") int categoryId);
    @POST("/api/basket/connect")
    Call<ConnectBasketResponse> connectToBasket(@Header("Authorization") String token, @Body ConnectBasketRequest request);
    @GET("/api/basket/content/{user_id}")
    Call<CartContentResponse> getBasketContents(@Path("user_id") int userId, @Header("Authorization") String token);
    @GET("/api/shopping_lists/{user_id}")
    Call<ShoppingListsResponse> getShoppingLists(@Path("user_id") int userId, @Header("Authorization") String token);
    @POST("/api/lists")
    Call<ShoppingList> createShoppingList(@Header("Authorization") String token, @Body CreateListRequest request);
    @PUT("/api/lists/{list_id}")
    Call<Void> renameShoppingList(@Header("Authorization") String token, @Path("list_id") int listId, @Body RenameListRequest request);
    @POST("/api/shopping-lists/{list_id}/contents")
    Call<Void> addToListContents(@Header("Authorization") String token, @Path("list_id") int listId, @Body AddToListRequest request);
    @GET("/api/favorites/{user_id}")
    Call<FavoritesResponse> getFavorites(@Path("user_id") int userId, @Header("Authorization") String token);
    @HTTP(method = "DELETE", path = "/api/favorites", hasBody = true)
    Call<Void> removeFromFavorites(@Header("Authorization") String token, @Body RemoveFromFavoritesRequest request);
    @GET("/api/list/content/{list_id}")
    Call<ListContentResponse> getListContents(@Path("list_id") int listId, @Header("Authorization") String token);
    @POST("/api/basket/finish")
    Call<FinishSessionResponse> finishSession(@Header("Authorization") String token, @Body FinishSessionRequest request);
    @POST("/api/list/content")
    Call<Void> addToListContents(@Header("Authorization") String token, @Body AddToListRequest request);
    @HTTP(method = "DELETE", path = "/api/basket/content", hasBody = true)
    Call<Void> removeFromBasket(@Header("Authorization") String token, @Body RemoveFromBasketRequest request);
    @GET("/api/list/content/{list_id}")
    Call<ListContentResponse> getListContents(@Header("Authorization") String token, @Path("list_id") int listId);
    @HTTP(method = "DELETE", path = "/api/list/content", hasBody = true)
    Call<Void> removeFromListContents(@Header("Authorization") String token, @Body RemoveFromListContentRequest request);
    @PATCH("/api/list/content/check")
    Call<Void> updateListItemChecked(@Header("Authorization") String token, @Body UpdateCheckedRequest request);
    @GET("/api/transactions/{user_id}")
    Call<TransactionResponse> getTransactionHistory(@Path("user_id") int userId, @Header("Authorization") String token);
    @PATCH("/api/list/content/quantity")
    Call<Void> updateListItemQuantity(@Header("Authorization") String token, @Body UpdateQuantityRequest request);
    @GET("/api/transaction/details/{transaction_id}")
    Call<TransactionDetailsResponse> getTransactionDetails(
            @Path("transaction_id") int transactionId,
            @Query("user_id") int userId,
            @Header("Authorization") String token
    );
}