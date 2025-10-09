package com.example.appconsqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ecommerce.db";

    // CRÍTICO: Se incrementa la versión de 1 a 2 para ejecutar onUpgrade
    private static final int DATABASE_VERSION = 2;

    public DBHelper(Context context) {
        // Al pasar DATABASE_VERSION (2), se activará onUpgrade si ya existe la DB con versión 1.
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla de usuarios (SENTENCIA MODIFICADA)
        // Se define aquí para asegurar que se crea antes que la tabla de productos, por la clave foránea.
        final String SQL_CREATE_USERS =
                "CREATE TABLE " + UserContract.UserEntry.TABLE_NAME + " (" +
                        UserContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        UserContract.UserEntry.COLUMN_EMAIL + " TEXT NOT NULL UNIQUE," +
                        UserContract.UserEntry.COLUMN_PASSWORD + " TEXT NOT NULL," +
                        // Nuevas columnas añadidas para el perfil
                        UserContract.UserEntry.COLUMN_NOMBRE + " TEXT," +
                        UserContract.UserEntry.COLUMN_APELLIDO + " TEXT," +
                        UserContract.UserEntry.COLUMN_TELEFONO + " TEXT," +
                        UserContract.UserEntry.COLUMN_DIRECCION + " TEXT," +
                        UserContract.UserEntry.COLUMN_FOTO_PERFIL_PATH + " TEXT)";

        // Crear tabla de productos (VERSIÓN ÚNICA Y CORREGIDA)
        // Se ha unificado la definición para incluir todas las columnas necesarias.
        final String SQL_CREATE_PRODUCTS =
                "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " (" +
                        ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ProductContract.ProductEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        ProductContract.ProductEntry.COLUMN_DESC + " TEXT, " +
                        ProductContract.ProductEntry.COLUMN_PRICE + " REAL NOT NULL, " +
                        ProductContract.ProductEntry.COLUMN_IMAGE_PATH + " TEXT, " + // Columna de imagen que faltaba
                        ProductContract.ProductEntry.COLUMN_USER_ID + " INTEGER, " +
                        "FOREIGN KEY(" + ProductContract.ProductEntry.COLUMN_USER_ID + ") REFERENCES " +
                        UserContract.UserEntry.TABLE_NAME + "(" + UserContract.UserEntry._ID + "))";

        // Se crean las tablas con las nuevas estructuras en el orden correcto
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_PRODUCTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Método de actualización simple (Borrar y Recrear)
        // Esto BORRARÁ todos los datos existentes, lo cual es común en desarrollo.
        db.execSQL("DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserContract.UserEntry.TABLE_NAME);
        onCreate(db);
    }
}
