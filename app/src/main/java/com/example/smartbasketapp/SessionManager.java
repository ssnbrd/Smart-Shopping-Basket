package com.example.smartbasketapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "SmartBasketSession";
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_SESSION_ID = "sessionId";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.commit();
    }

    public String getAuthToken() {
        return pref.getString(KEY_AUTH_TOKEN, null);
    }

    public void saveUserId(int userId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.commit();
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }
    public void saveSessionId(int sessionId) {
        editor.putInt(KEY_SESSION_ID, sessionId);
        editor.commit();
    }
    public int getSessionId() {
        return pref.getInt(KEY_SESSION_ID, -1);
    }
    public void clearSession() {
        editor.remove(KEY_SESSION_ID);
        editor.commit();
    }
    public void clear() {
        editor.clear();
        editor.commit();
    }
}