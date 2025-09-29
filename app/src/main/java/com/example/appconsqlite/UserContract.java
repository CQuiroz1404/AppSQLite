package com.example.appconsqlite;

import android.provider.BaseColumns;

public final class UserContract {
    private UserContract() {}

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "usuarios";

        // Columnas de Login existentes
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_APELLIDO = "apellido";
        public static final String COLUMN_TELEFONO = "telefono";
        public static final String COLUMN_DIRECCION = "direccion_principal";
        public static final String COLUMN_FOTO_PERFIL_PATH = "foto_perfil_path";
        // Esta columna puede almacenar la URI o ruta del archivo de la foto.
        // ===================================================
    }
}