package com.example.appconsqlite.data.database;

import android.provider.BaseColumns;

// Define la estructura de la tabla de productos y categorías disponibles
public class ProductContract {
    private ProductContract() {}

    public static class ProductEntry implements BaseColumns {
        public static final String TABLE_NAME = "productos";
        public static final String COLUMN_NAME = "nombre";
        public static final String COLUMN_DESC = "descripcion";
        public static final String COLUMN_PRICE = "precio";
        public static final String COLUMN_IMAGE_PATH = "imagen";
        public static final String COLUMN_USER_ID = "usuario_id";
        public static final String COLUMN_QUANTITY = "cantidad";
        public static final String COLUMN_CATEGORY = "categoria";
    }

    // Categorías predefinidas para clasificar productos en el marketplace
    public static class Categories {
        public static final String ELECTRONICA = "Electrónica";
        public static final String ROPA = "Ropa y Accesorios";
        public static final String HOGAR = "Hogar y Jardín";
        public static final String DEPORTES = "Deportes";
        public static final String LIBROS = "Libros y Medios";
        public static final String JUGUETES = "Juguetes";
        public static final String ALIMENTOS = "Alimentos y Bebidas";
        public static final String BELLEZA = "Belleza y Cuidado Personal";
        public static final String AUTOMOTRIZ = "Automotriz";
        public static final String OTROS = "Otros";

        // Retorna array con todas las categorías disponibles para filtros
        public static String[] getAllCategories() {
            return new String[]{
                ELECTRONICA, ROPA, HOGAR, DEPORTES, LIBROS,
                JUGUETES, ALIMENTOS, BELLEZA, AUTOMOTRIZ, OTROS
            };
        }
    }
}

