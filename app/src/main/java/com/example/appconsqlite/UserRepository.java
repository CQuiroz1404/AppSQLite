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

    // Función para hashear contraseña con SHA-256
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

    // Registrar usuario
    public boolean registrarUsuario(String email, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_EMAIL, email);
        values.put(UserContract.UserEntry.COLUMN_PASSWORD, hashPassword(password));

        long result = db.insert(UserContract.UserEntry.TABLE_NAME, null, values);
        db.close();

        return result != -1; // true si se registró correctamente
    }

    // Verificar login
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
}
