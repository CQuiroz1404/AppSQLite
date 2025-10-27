package com.example.appconsqlite.data.database;

import android.provider.BaseColumns;

// Define la estructura de la tabla de usuarios en la base de datos
public final class UserContract {
    private UserContract() {}

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "usuarios";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_APELLIDO = "apellido";
        public static final String COLUMN_TELEFONO = "telefono";
        public static final String COLUMN_DIRECCION = "direccion_principal";
        public static final String COLUMN_FOTO_PERFIL_PATH = "foto_perfil_path";
    }
}

