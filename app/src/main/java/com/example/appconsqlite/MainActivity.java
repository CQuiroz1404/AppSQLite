package com.example.appconsqlite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button btnLogin, btnRegister;
    private UserRepository userRepo;

    // Constantes para SharedPreferences
    private static final String PREFS_FILE = "com.example.appconsqlite.PREFERENCE_FILE_KEY";
    private static final String IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ===============================================
        // LÓGICA DE PERSISTENCIA DE SESIÓN: Comprobar estado
        // ===============================================
        SharedPreferences sharedPref = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean(IS_LOGGED_IN, false);

        if (isLoggedIn) {
            // Si ya está logeado, ir directamente al menú
            Intent intent = new Intent(MainActivity.this, Menu.class);
            startActivity(intent);
            finish(); // Cierra esta actividad para que no se pueda volver
            return; // Termina onCreate aquí
        }
        // ===============================================

        // Activar Edge-to-Edge si no ha retornado
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Inicializar repositorio
        userRepo = new UserRepository(this);

        // Acción de login
        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean logeado = userRepo.loginUsuario(email, password);
            if (logeado) {
                Toast.makeText(this, "Login correcto", Toast.LENGTH_SHORT).show();

                // ===============================================
                // GUARDAR ESTADO DE SESIÓN al hacer Login
                // ===============================================
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(IS_LOGGED_IN, true);
                editor.apply();

                // Abrir Menu Activity
                Intent intent = new Intent(MainActivity.this, Menu.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });


        // Acción de abrir Activity de registro
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Registro.class);
            startActivity(intent);
        });
    }
}