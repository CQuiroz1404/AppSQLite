package com.example.appconsqlite;

import android.provider.BaseColumns;
public class ProductContract {
    private ProductContract() {}

    public static class ProductEntry implements BaseColumns {
        public static final String TABLE_NAME = "productos";
        public static final String COLUMN_NAME = "nombre";
        public static final String COLUMN_DESC = "descripcion";
        public static final String COLUMN_PRICE = "precio";
        public static final String COLUMN_IMAGE_PATH = "imagen";
        public static final String COLUMN_USER_ID = "usuario_id";
    }
}
