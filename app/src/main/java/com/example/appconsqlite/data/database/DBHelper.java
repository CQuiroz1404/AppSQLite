package com.example.appconsqlite.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "marketplace.db";

    // Incrementar versión para forzar actualización con categorías
    private static final int DATABASE_VERSION = 6;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla de usuarios
        // Se define aquí para asegurar que se crea antes que la tabla de productos.
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

        // Se ha añadido la columna CATEGORIA
        final String SQL_CREATE_PRODUCTS =
                "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " (" +
                        ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ProductContract.ProductEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        ProductContract.ProductEntry.COLUMN_DESC + " TEXT, " +
                        ProductContract.ProductEntry.COLUMN_PRICE + " REAL NOT NULL, " +
                        ProductContract.ProductEntry.COLUMN_IMAGE_PATH + " TEXT, " +
                        ProductContract.ProductEntry.COLUMN_QUANTITY + " INTEGER DEFAULT 1, " +
                        ProductContract.ProductEntry.COLUMN_CATEGORY + " TEXT DEFAULT 'Otros', " + // Nueva columna
                        ProductContract.ProductEntry.COLUMN_USER_ID + " INTEGER, " +
                        "FOREIGN KEY(" + ProductContract.ProductEntry.COLUMN_USER_ID + ") REFERENCES " +
                        UserContract.UserEntry.TABLE_NAME + "(" + UserContract.UserEntry._ID + "))";

        // Se crean las tablas con las nuevas estructuras en el orden correcto
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_PRODUCTS);

        // Insertar productos de ejemplo (también actualizados)
        insertarProductosDeEjemplo(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Esto BORRARÁ todos los datos existentes.
        db.execSQL("DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserContract.UserEntry.TABLE_NAME);
        onCreate(db);
    }


    // Se añade el campo de cantidad a cada producto.
    private void insertarProductosDeEjemplo(SQLiteDatabase db) {
        // Producto 1: Laptop Gaming (en pesos chilenos)
        ContentValues laptop = new ContentValues();
        laptop.put(ProductContract.ProductEntry.COLUMN_NAME, "Laptop Gaming MSI");
        laptop.put(ProductContract.ProductEntry.COLUMN_DESC, "Laptop MSI GF63, Intel Core i7, 16GB RAM, RTX 3050, 512GB SSD. Perfecta para gaming y diseño.");
        laptop.put(ProductContract.ProductEntry.COLUMN_PRICE, 1099000); // ~1.1 millones CLP
        laptop.put(ProductContract.ProductEntry.COLUMN_IMAGE_PATH, ""); // Sin imagen
        laptop.put(ProductContract.ProductEntry.COLUMN_USER_ID, 1); // Usuario demo
        laptop.put(ProductContract.ProductEntry.COLUMN_QUANTITY, 5); // Cantidad de ejemplo
        laptop.put(ProductContract.ProductEntry.COLUMN_CATEGORY, ProductContract.Categories.ELECTRONICA); // Nueva columna
        db.insert(ProductContract.ProductEntry.TABLE_NAME, null, laptop);

        // Producto 2: Monitor 27" (en pesos chilenos)
        ContentValues monitor = new ContentValues();
        monitor.put(ProductContract.ProductEntry.COLUMN_NAME, "Monitor LG 27\" 4K");
        monitor.put(ProductContract.ProductEntry.COLUMN_DESC, "Monitor LG UltraFine 27 pulgadas, resolución 4K UHD, IPS, 60Hz. Ideal para productividad y multimedia.");
        monitor.put(ProductContract.ProductEntry.COLUMN_PRICE, 349990); // ~350mil CLP
        monitor.put(ProductContract.ProductEntry.COLUMN_IMAGE_PATH, "");
        monitor.put(ProductContract.ProductEntry.COLUMN_USER_ID, 1);
        monitor.put(ProductContract.ProductEntry.COLUMN_QUANTITY, 10); // Cantidad de ejemplo
        monitor.put(ProductContract.ProductEntry.COLUMN_CATEGORY, ProductContract.Categories.ELECTRONICA); // Nueva columna
        db.insert(ProductContract.ProductEntry.TABLE_NAME, null, monitor);

        // Producto 3: Teclado Mecánico (en pesos chilenos)
        ContentValues teclado = new ContentValues();
        teclado.put(ProductContract.ProductEntry.COLUMN_NAME, "Teclado Mecánico RGB");
        teclado.put(ProductContract.ProductEntry.COLUMN_DESC, "Teclado mecánico gaming, switches Cherry MX Red, retroiluminación RGB personalizable, reposamuñecas incluido.");
        teclado.put(ProductContract.ProductEntry.COLUMN_PRICE, 79990); // ~80mil CLP
        teclado.put(ProductContract.ProductEntry.COLUMN_IMAGE_PATH, "");
        teclado.put(ProductContract.ProductEntry.COLUMN_USER_ID, 1);
        teclado.put(ProductContract.ProductEntry.COLUMN_QUANTITY, 20); // Cantidad de ejemplo
        teclado.put(ProductContract.ProductEntry.COLUMN_CATEGORY, ProductContract.Categories.ELECTRONICA); // Nueva columna
        db.insert(ProductContract.ProductEntry.TABLE_NAME, null, teclado);
    }
}

