package com.example.appconsqlite.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

// Gestiona el hash y verificación segura de contraseñas usando BCrypt
public class PasswordHasher {

    private static final int BCRYPT_COST = 12;

    // Genera hash seguro de contraseña usando BCrypt con cost factor 12
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray());
    }

    // Verifica si una contraseña coincide con su hash almacenado
    public static boolean checkPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }

        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
        return result.verified;
    }
}

