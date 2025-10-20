package com.example.appconsqlite.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Clase helper para gestionar el hash y verificación de contraseñas usando BCrypt.
 * Actualizado para usar la librería moderna at.favre.lib.bcrypt con mejor rendimiento.
 */
public class PasswordHasher {

    // Cost factor para BCrypt (12 es un buen balance entre seguridad y rendimiento)
    private static final int BCRYPT_COST = 12;

    /**
     * Genera un hash seguro de la contraseña usando BCrypt.
     * @param password Contraseña en texto plano
     * @return Hash BCrypt de la contraseña
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray());
    }

    /**
     * Verifica si una contraseña coincide con un hash BCrypt almacenado.
     * @param password Contraseña en texto plano a verificar
     * @param hashedPassword Hash BCrypt almacenado
     * @return true si la contraseña coincide, false en caso contrario
     */
    public static boolean checkPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }

        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
        return result.verified;
    }
}

