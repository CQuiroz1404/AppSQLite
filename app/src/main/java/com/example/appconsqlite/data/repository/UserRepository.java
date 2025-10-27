package com.example.appconsqlite.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.appconsqlite.data.database.DBHelper;
import com.example.appconsqlite.data.database.UserContract;
import com.example.appconsqlite.data.database.ProductContract;
import com.example.appconsqlite.utils.PasswordHasher;

public class UserRepository {
    private DBHelper dbHelper;

    public UserRepository(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    // Registrar usuario con BCrypt
    public boolean registrarUsuario(String nombre, String apellido, String email, String password, String telefono, String direccion, String fotoPath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_EMAIL, email);
        values.put(UserContract.UserEntry.COLUMN_PASSWORD, PasswordHasher.hashPassword(password));

        // CAMPOS DE PERFIL
        values.put(UserContract.UserEntry.COLUMN_NOMBRE, nombre);
        values.put(UserContract.UserEntry.COLUMN_APELLIDO, apellido);
        values.put(UserContract.UserEntry.COLUMN_TELEFONO, telefono);
        values.put(UserContract.UserEntry.COLUMN_DIRECCION, direccion);
        values.put(UserContract.UserEntry.COLUMN_FOTO_PERFIL_PATH, fotoPath);

        long result = db.insert(UserContract.UserEntry.TABLE_NAME, null, values);
        db.close();

        return result != -1;
    }

    // Verificar login con BCrypt
    public boolean loginUsuario(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
            UserContract.UserEntry._ID,
            UserContract.UserEntry.COLUMN_PASSWORD
        };
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

        boolean existe = false;
        if (cursor.moveToFirst()) {
            String hashedPassword = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_PASSWORD));
            existe = PasswordHasher.checkPassword(password, hashedPassword);
        }

        cursor.close();
        db.close();

        return existe;
    }

    // Obtener ID del usuario por email (NUEVO MÉTODO)
    public long getUserIdByEmail(String email) {
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
    
    // Obtener ID del usuario por email
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

    // Obtener todos los datos del perfil
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

        return db.query(
                UserContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    // Actualizar datos del perfil
    public boolean actualizarPerfil(long userId, String nombre, String apellido, String telefono, String direccion, String fotoPath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_NOMBRE, nombre);
        values.put(UserContract.UserEntry.COLUMN_APELLIDO, apellido);
        values.put(UserContract.UserEntry.COLUMN_TELEFONO, telefono);
        values.put(UserContract.UserEntry.COLUMN_DIRECCION, direccion);
        if (fotoPath != null && !fotoPath.isEmpty()) {
            values.put(UserContract.UserEntry.COLUMN_FOTO_PERFIL_PATH, fotoPath);
        }

        int rows = db.update(
                UserContract.UserEntry.TABLE_NAME,
                values,
                UserContract.UserEntry._ID + "=?",
                new String[]{ String.valueOf(userId) }
        );
        db.close();

        return rows > 0;
    }

    // Cambiar contraseña
    public boolean cambiarPassword(long userId, String passwordAntigua, String passwordNueva) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Verificar contraseña antigua
        String[] projection = { UserContract.UserEntry.COLUMN_PASSWORD };
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

        boolean passwordCorrecta = false;
        if (cursor.moveToFirst()) {
            String hashedPassword = cursor.getString(0);
            passwordCorrecta = PasswordHasher.checkPassword(passwordAntigua, hashedPassword);
        }
        cursor.close();

        if (!passwordCorrecta) {
            db.close();
            return false;
        }

        // Actualizar con nueva contraseña
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_PASSWORD, PasswordHasher.hashPassword(passwordNueva));

        int rows = db.update(
                UserContract.UserEntry.TABLE_NAME,
                values,
                UserContract.UserEntry._ID + "=?",
                new String[]{ String.valueOf(userId) }
        );
        db.close();

        return rows > 0;
    }

    // Eliminar cuenta de usuario
    public boolean eliminarCuenta(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Primero eliminar todos los productos del usuario
        db.delete(
                ProductContract.ProductEntry.TABLE_NAME,
                ProductContract.ProductEntry.COLUMN_USER_ID + "=?",
                new String[]{ String.valueOf(userId) }
        );

        // Luego eliminar el usuario
        int filas = db.delete(
                UserContract.UserEntry.TABLE_NAME,
                UserContract.UserEntry._ID + "=?",
                new String[]{ String.valueOf(userId) }
        );
        db.close();

        return filas > 0;
    }

    // Actualizar email
    public boolean actualizarEmail(long userId, String nuevoEmail) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_EMAIL, nuevoEmail);

        int rows = db.update(
                UserContract.UserEntry.TABLE_NAME,
                values,
                UserContract.UserEntry._ID + "=?",
                new String[]{ String.valueOf(userId) }
        );
        db.close();

        return rows > 0;
    }
}
