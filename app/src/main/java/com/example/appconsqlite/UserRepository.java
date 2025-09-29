package com.example.appconsqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.security.MessageDigest;

public class UserRepository {
    private DBHelper dbHelper;

    public UserRepository(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    // Función para hashear contraseña con SHA-256 (Se mantiene igual)
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // Registrar usuario - MODIFICADO para incluir nuevos campos con valores por defecto
    // Se asume que el Activity de Registro solo proporciona email y password.
    public boolean registrarUsuario(String nombre, String apellido, String email, String password, String telefono, String direccion, String fotoPath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_EMAIL, email);
        values.put(UserContract.UserEntry.COLUMN_PASSWORD, hashPassword(password));

        // CAMPOS DE PERFIL ACTUALIZADOS
        values.put(UserContract.UserEntry.COLUMN_NOMBRE, nombre);
        values.put(UserContract.UserEntry.COLUMN_APELLIDO, apellido);
        values.put(UserContract.UserEntry.COLUMN_TELEFONO, telefono); // Ahora se pasa
        values.put(UserContract.UserEntry.COLUMN_DIRECCION, direccion); // Ahora se pasa
        values.put(UserContract.UserEntry.COLUMN_FOTO_PERFIL_PATH, fotoPath); // Ahora se pasa

        long result = db.insert(UserContract.UserEntry.TABLE_NAME, null, values);
        db.close();

        return result != -1;
    }

    // Verificar login (Se mantiene igual)
    public boolean loginUsuario(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = { UserContract.UserEntry._ID };
        String selection = UserContract.UserEntry.COLUMN_EMAIL + "=? AND " +
                UserContract.UserEntry.COLUMN_PASSWORD + "=?";
        String[] selectionArgs = { email, hashPassword(password) };

        Cursor cursor = db.query(
                UserContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean existe = cursor.moveToFirst();
        cursor.close();
        db.close();

        return existe;
    }

    // =====================================================================
    // NUEVA FUNCIÓN: Obtener ID y otros datos del usuario logeado
    // Nota: Es mejor guardar el ID en SharedPreferences después del login,
    // pero aquí devolvemos todos los datos asociados a un email.
    // =====================================================================

    /**
     * Busca y devuelve el ID único (Primary Key) de un usuario por su email.
     * @param email El email del usuario.
     * @return El ID del usuario (Long), o -1 si no se encuentra.
     */
    public long obtenerUserId(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long userId = -1;

        String[] projection = { UserContract.UserEntry._ID };
        String selection = UserContract.UserEntry.COLUMN_EMAIL + "=?";
        String[] selectionArgs = { email };

        Cursor cursor = db.query(
                UserContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(UserContract.UserEntry._ID));
        }

        cursor.close();
        db.close();
        return userId;
    }

    // =====================================================================
    // Función para obtener todos los datos del perfil
    // Para simplificar, devolvemos un Cursor. En una app real, usarías una clase 'User' o 'Perfil'.
    // =====================================================================

    /**
     * Obtiene todos los datos de perfil para un ID de usuario específico.
     * @param userId El ID del usuario.
     * @return Un Cursor con los datos del perfil, o null si hay error. El caller debe cerrar el Cursor.
     */
    public Cursor obtenerDatosPerfil(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                UserContract.UserEntry.COLUMN_NOMBRE,
                UserContract.UserEntry.COLUMN_APELLIDO,
                UserContract.UserEntry.COLUMN_EMAIL,
                UserContract.UserEntry.COLUMN_TELEFONO,
                UserContract.UserEntry.COLUMN_DIRECCION,
                UserContract.UserEntry.COLUMN_FOTO_PERFIL_PATH
        };

        String selection = UserContract.UserEntry._ID + "=?";
        String[] selectionArgs = { String.valueOf(userId) };

        Cursor cursor = db.query(
                UserContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        // Nota: No cerramos la base de datos ni el cursor aquí, ya que el Activity que llama
        // necesitará el Cursor abierto para leer los datos.
        return cursor;
    }

}