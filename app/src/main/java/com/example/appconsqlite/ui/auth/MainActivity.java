package com.example.appconsqlite.ui.auth;

import android.content.Intent;
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

import com.example.appconsqlite.ui.main.Menu;
import com.example.appconsqlite.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.appconsqlite.data.repository.UserRepository;
import com.example.appconsqlite.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editPassword;
    private TextInputLayout tilEmail, tilPassword;
    private Button btnLogin, btnRegister;
    private UserRepository userRepo;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // FORZAR MODO CLARO - Desactivar modo oscuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);

        // Inicializar SessionManager con encriptación
        sessionManager = new SessionManager(this);

        // Verificar si ya hay una sesión activa
        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, Menu.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Configurar la barra de estado con color rojo
        getWindow().setStatusBarColor(getResources().getColor(R.color.red_primary, null));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background_light_gray, null));

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
            sessionManager.clearAll();
            Toast.makeText(this, "Sesión limpiada. Puedes hacer pruebas nuevamente.", Toast.LENGTH_LONG).show();
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
                    // Usar SessionManager para guardar la sesión de forma segura
                    sessionManager.createSession(userId, email);

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