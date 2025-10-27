package com.example.appconsqlite.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Helper de base de datos para gestionar la creación y actualización de la BD del marketplace
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "marketplace.db";
    private static final int DATABASE_VERSION = 6;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla de usuarios con campos de perfil
        final String SQL_CREATE_USERS =
                "CREATE TABLE " + UserContract.UserEntry.TABLE_NAME + " (" +
                        UserContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        UserContract.UserEntry.COLUMN_EMAIL + " TEXT NOT NULL UNIQUE," +
                        UserContract.UserEntry.COLUMN_PASSWORD + " TEXT NOT NULL," +
                        UserContract.UserEntry.COLUMN_NOMBRE + " TEXT," +
                        UserContract.UserEntry.COLUMN_APELLIDO + " TEXT," +
                        UserContract.UserEntry.COLUMN_TELEFONO + " TEXT," +
                        UserContract.UserEntry.COLUMN_DIRECCION + " TEXT," +
                        UserContract.UserEntry.COLUMN_FOTO_PERFIL_PATH + " TEXT)";

        // Crear tabla de productos con categoría y relación a usuarios
        final String SQL_CREATE_PRODUCTS =
                "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " (" +
                        ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ProductContract.ProductEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        ProductContract.ProductEntry.COLUMN_DESC + " TEXT, " +
                        ProductContract.ProductEntry.COLUMN_PRICE + " REAL NOT NULL, " +
                        ProductContract.ProductEntry.COLUMN_IMAGE_PATH + " TEXT, " +
                        ProductContract.ProductEntry.COLUMN_QUANTITY + " INTEGER DEFAULT 1, " +
                        ProductContract.ProductEntry.COLUMN_CATEGORY + " TEXT DEFAULT 'Otros', " +
                        ProductContract.ProductEntry.COLUMN_USER_ID + " INTEGER, " +
                        "FOREIGN KEY(" + ProductContract.ProductEntry.COLUMN_USER_ID + ") REFERENCES " +
                        UserContract.UserEntry.TABLE_NAME + "(" + UserContract.UserEntry._ID + "))";

        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_PRODUCTS);
        insertarProductosDeEjemplo(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Eliminar tablas existentes y recrear la base de datos
        db.execSQL("DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserContract.UserEntry.TABLE_NAME);
        onCreate(db);
    }

    // Inserta 3 productos de ejemplo de electrónica al crear la base de datos
    private void insertarProductosDeEjemplo(SQLiteDatabase db) {
        ContentValues laptop = new ContentValues();
        laptop.put(ProductContract.ProductEntry.COLUMN_NAME, "Laptop Gaming MSI");
        laptop.put(ProductContract.ProductEntry.COLUMN_DESC, "Laptop MSI GF63, Intel Core i7, 16GB RAM, RTX 3050, 512GB SSD. Perfecta para gaming y diseño.");
        laptop.put(ProductContract.ProductEntry.COLUMN_PRICE, 1099000);
        laptop.put(ProductContract.ProductEntry.COLUMN_IMAGE_PATH, "");
        laptop.put(ProductContract.ProductEntry.COLUMN_USER_ID, 1);
        laptop.put(ProductContract.ProductEntry.COLUMN_QUANTITY, 5);
        laptop.put(ProductContract.ProductEntry.COLUMN_CATEGORY, ProductContract.Categories.ELECTRONICA);
        db.insert(ProductContract.ProductEntry.TABLE_NAME, null, laptop);

        ContentValues monitor = new ContentValues();
        monitor.put(ProductContract.ProductEntry.COLUMN_NAME, "Monitor LG 27\" 4K");
        monitor.put(ProductContract.ProductEntry.COLUMN_DESC, "Monitor LG UltraFine 27 pulgadas, resolución 4K UHD, IPS, 60Hz. Ideal para productividad y multimedia.");
        monitor.put(ProductContract.ProductEntry.COLUMN_PRICE, 349990);
        monitor.put(ProductContract.ProductEntry.COLUMN_IMAGE_PATH, "");
        monitor.put(ProductContract.ProductEntry.COLUMN_USER_ID, 1);
        monitor.put(ProductContract.ProductEntry.COLUMN_QUANTITY, 10);
        monitor.put(ProductContract.ProductEntry.COLUMN_CATEGORY, ProductContract.Categories.ELECTRONICA);
        db.insert(ProductContract.ProductEntry.TABLE_NAME, null, monitor);

        ContentValues teclado = new ContentValues();
        teclado.put(ProductContract.ProductEntry.COLUMN_NAME, "Teclado Mecánico RGB");
        teclado.put(ProductContract.ProductEntry.COLUMN_DESC, "Teclado mecánico gaming, switches Cherry MX Red, retroiluminación RGB personalizable, reposamuñecas incluido.");
        teclado.put(ProductContract.ProductEntry.COLUMN_PRICE, 79990);
        teclado.put(ProductContract.ProductEntry.COLUMN_IMAGE_PATH, "");
        teclado.put(ProductContract.ProductEntry.COLUMN_USER_ID, 1);
        teclado.put(ProductContract.ProductEntry.COLUMN_QUANTITY, 20);
        teclado.put(ProductContract.ProductEntry.COLUMN_CATEGORY, ProductContract.Categories.ELECTRONICA);
        db.insert(ProductContract.ProductEntry.TABLE_NAME, null, teclado);
    }
}

