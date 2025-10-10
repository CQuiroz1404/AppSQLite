package com.example.appconsqlite;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    // Genera un hash de la contraseña usando BCrypt
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Verifica si la contraseña ingresada coincide con el hash almacenado
    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}
