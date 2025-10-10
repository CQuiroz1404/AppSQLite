package com.example.appconsqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ProductRepository {
    private DBHelper dbHelper;

    public ProductRepository(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    // ==========================================================
    // 🔹 Insertar nuevo producto
    // ==========================================================
    public boolean insertarProducto(String nombre, String descripcion, double precio, long userId, String imagenPath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_NAME, nombre);
        values.put(ProductContract.ProductEntry.COLUMN_DESC, descripcion);
        values.put(ProductContract.ProductEntry.COLUMN_PRICE, precio);
        values.put(ProductContract.ProductEntry.COLUMN_USER_ID, userId);
        values.put(ProductContract.ProductEntry.COLUMN_IMAGE_PATH, imagenPath);

        long result = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);
        db.close();

        return result != -1;
    }

    // ==========================================================
    // 🔹 Actualizar producto existente
    // ==========================================================
    public boolean actualizarProducto(long productId, String nombre, String descripcion, double precio, String imagenPath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_NAME, nombre);
        values.put(ProductContract.ProductEntry.COLUMN_DESC, descripcion);
        values.put(ProductContract.ProductEntry.COLUMN_PRICE, precio);
        if (imagenPath != null && !imagenPath.isEmpty()) {
            values.put(ProductContract.ProductEntry.COLUMN_IMAGE_PATH, imagenPath);
        }

        int rows = db.update(
                ProductContract.ProductEntry.TABLE_NAME,
                values,
                ProductContract.ProductEntry._ID + "=?",
                new String[]{ String.valueOf(productId) }
        );
        db.close();

        return rows > 0;
    }

    // ==========================================================
    // 🔹 Obtener todos los productos
    // ==========================================================
    public Cursor obtenerTodosLosProductos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_NAME,
                ProductContract.ProductEntry.COLUMN_DESC,
                ProductContract.ProductEntry.COLUMN_PRICE,
                ProductContract.ProductEntry.COLUMN_IMAGE_PATH,
                ProductContract.ProductEntry.COLUMN_USER_ID
        };

        return db.query(
                ProductContract.ProductEntry.TABLE_NAME,
                projection,
                null, null, null, null,
                ProductContract.ProductEntry._ID + " DESC" // más nuevos primero
        );
    }

    // ==========================================================
    // 🔹 Obtener un producto específico por ID
    // ==========================================================
    public Cursor obtenerProductoPorId(long productId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_NAME,
                ProductContract.ProductEntry.COLUMN_DESC,
                ProductContract.ProductEntry.COLUMN_PRICE,
                ProductContract.ProductEntry.COLUMN_IMAGE_PATH,
                ProductContract.ProductEntry.COLUMN_USER_ID
        };

        String selection = ProductContract.ProductEntry._ID + "=?";
        String[] selectionArgs = { String.valueOf(productId) };

        return db.query(
                ProductContract.ProductEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    // ==========================================================
    // 🔹 Obtener productos por usuario específico
    // ==========================================================
    public Cursor obtenerProductosPorUsuario(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_NAME,
                ProductContract.ProductEntry.COLUMN_DESC,
                ProductContract.ProductEntry.COLUMN_PRICE,
                ProductContract.ProductEntry.COLUMN_IMAGE_PATH,
                ProductContract.ProductEntry.COLUMN_USER_ID
        };

        String selection = ProductContract.ProductEntry.COLUMN_USER_ID + "=?";
        String[] selectionArgs = { String.valueOf(userId) };

        return db.query(
                ProductContract.ProductEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                ProductContract.ProductEntry._ID + " DESC"
        );
    }

    // ==========================================================
    // 🔹 Eliminar producto (por ID)
    // ==========================================================
    public boolean eliminarProducto(long productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int filas = db.delete(
                ProductContract.ProductEntry.TABLE_NAME,
                ProductContract.ProductEntry._ID + "=?",
                new String[]{ String.valueOf(productId) }
        );
        db.close();
        return filas > 0;
    }

    // ==========================================================
    // 🔹 Verificar si el usuario es dueño del producto
    // ==========================================================
    public boolean esProductoDelUsuario(long productId, long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = { ProductContract.ProductEntry._ID };
        String selection = ProductContract.ProductEntry._ID + "=? AND " +
                          ProductContract.ProductEntry.COLUMN_USER_ID + "=?";
        String[] selectionArgs = { String.valueOf(productId), String.valueOf(userId) };

        Cursor cursor = db.query(
                ProductContract.ProductEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean esDelUsuario = cursor.moveToFirst();
        cursor.close();
        db.close();

        return esDelUsuario;
    }
}
