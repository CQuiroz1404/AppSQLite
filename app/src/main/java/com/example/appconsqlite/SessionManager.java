package com.example.appconsqlite;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Clase para gestionar la sesión del usuario de forma centralizada.
 * Versión simplificada que usa SharedPreferences normales.
 * TODO: Actualizar a EncryptedSharedPreferences cuando se descarguen las dependencias.
 */
public class SessionManager {

    private static final String PREFS_FILE = "user_session_prefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";

    private final SharedPreferences preferences;

    /**
     * Constructor que inicializa SharedPreferences.
     * @param context Contexto de la aplicación
     */
    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    }

    /**
     * Guarda los datos de sesión del usuario.
     * @param userId ID del usuario
     * @param email Email del usuario
     */
    public void createSession(long userId, String email) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    /**
     * Verifica si hay una sesión activa.
     * @return true si el usuario está logueado
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Obtiene el ID del usuario actual.
     * @return ID del usuario o -1 si no hay sesión
     */
    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1);
    }

    /**
     * Obtiene el email del usuario actual.
     * @return Email del usuario o null si no hay sesión
     */
    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Cierra la sesión actual y limpia todos los datos.
     */
    public void logout() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Limpia completamente las preferencias (útil para debugging).
     */
    public void clearAll() {
        preferences.edit().clear().apply();
    }
}
