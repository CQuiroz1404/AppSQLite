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
    // 🔹 Obtener productos por usuario específico
    // ==========================================================
    public Cursor obtenerProductosPorUsuario(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_NAME,
                ProductContract.ProductEntry.COLUMN_DESC,
                ProductContract.ProductEntry.COLUMN_PRICE,
                ProductContract.ProductEntry.COLUMN_IMAGE_PATH
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
}
