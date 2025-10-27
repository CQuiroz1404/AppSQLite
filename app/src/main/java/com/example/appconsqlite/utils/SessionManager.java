package com.example.appconsqlite.utils;

import android.content.Context;
import android.content.SharedPreferences;

// Gestiona la sesión del usuario usando SharedPreferences
public class SessionManager {

    private static final String PREFS_FILE = "user_session_prefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    }

    // Guarda datos de sesión al iniciar sesión
    public void createSession(long userId, String email) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    // Verifica si existe sesión activa
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Retorna ID del usuario actual o -1 si no hay sesión
    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1);
    }

    // Retorna email del usuario actual
    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }

    // Cierra sesión y limpia preferencias
    public void logout() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    // Limpia todas las preferencias (útil para debugging)
    public void clearAll() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    // Actualiza email en sesión actual
    public void updateEmail(String newEmail) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_EMAIL, newEmail);
        editor.apply();
    }
}
