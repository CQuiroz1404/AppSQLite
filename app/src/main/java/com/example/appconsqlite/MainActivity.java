package com.example.appconsqlite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editPassword;
    private TextInputLayout tilEmail, tilPassword;
    private Button btnLogin, btnRegister;
    private UserRepository userRepo;

    // Constantes para SharedPreferences
    private static final String PREFS_FILE = "com.example.appconsqlite.PREFERENCE_FILE_KEY";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private static final String USER_ID = "userId";
    private static final String USER_EMAIL = "userEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // FORZAR MODO CLARO - Desactivar modo oscuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean(IS_LOGGED_IN, false);

        if (isLoggedIn) {
            Intent intent = new Intent(MainActivity.this, Menu.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        tilEmail = findViewById(R.id.tilEmail);
        editEmail = findViewById(R.id.editEmail);
        tilPassword = findViewById(R.id.tilPassword);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Inicializar repositorio
        userRepo = new UserRepository(this);

        // DEBUG: Long-click en el logo para limpiar preferencias guardadas (solo para desarrollo)
        findViewById(R.id.ivLogo).setOnLongClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();
            Toast.makeText(this, "Preferencias borradas. Puedes hacer pruebas nuevamente.", Toast.LENGTH_LONG).show();
            return true;
        });

        // Acción de login
        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            // Resetear errores
            tilEmail.setError(null);
            tilPassword.setError(null);

            boolean isValid = true;

            if (TextUtils.isEmpty(email)) {
                tilEmail.setError("El email no puede estar vacío");
                isValid = false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.setError("Formato de email inválido");
                isValid = false;
            }

            if (TextUtils.isEmpty(password)) {
                tilPassword.setError("La contraseña no puede estar vacía");
                isValid = false;
            }

            if (!isValid) {
                return;
            }

            // Lógica de login con BCrypt
            boolean logeado = userRepo.loginUsuario(email, password);
            if (logeado) {
                Toast.makeText(this, "¡Bienvenido al Marketplace!", Toast.LENGTH_SHORT).show();

                long userId = userRepo.obtenerUserId(email);

                if (userId != -1) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(IS_LOGGED_IN, true);
                    editor.putLong(USER_ID, userId);
                    editor.putString(USER_EMAIL, email);
                    editor.apply();

                    Intent intent = new Intent(MainActivity.this, Menu.class);
                    startActivity(intent);
                    finish();
                } else {
                    tilEmail.setError("Error al obtener datos de usuario");
                }
            } else {
                tilPassword.setError("Email o contraseña incorrectos");
            }
        });

        // Acción de abrir Activity de registro
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Registro.class);
            startActivity(intent);
        });
    }
}