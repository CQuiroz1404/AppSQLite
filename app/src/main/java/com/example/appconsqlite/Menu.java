package com.example.appconsqlite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import androidx.activity.OnBackPressedCallback;

public class Menu extends AppCompatActivity {

    // Constantes para SharedPreferences (copiadas de MainActivity)
    private static final String PREFS_FILE = "com.example.appconsqlite.PREFERENCE_FILE_KEY";
    private static final String IS_LOGGED_IN = "isLoggedIn";

    // Variables no usadas, pero se mantienen si son necesarias para otra lógica
    int numero;
    int num1;
    int num3;
    int num2;

    // Declaración de botones
    private Button btnAdd1, btnAdd2, btnAdd3, btnAdd4, btnNavCerrarSesion; // Añadido btnNavCerrarSesion
    private ImageButton btnMenu; // Botón para el menú/aside

    // Declaración del DrawerLayout
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        // 1. Inicializar el DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // 2. Inicializar botones de la vista
        btnAdd1 = findViewById(R.id.btnAdd1);
        btnAdd2 = findViewById(R.id.btnAdd2);
        btnAdd3 = findViewById(R.id.btnAdd3);
        btnAdd4 = findViewById(R.id.btnAdd4);
        btnMenu = findViewById(R.id.btnMenu);

        // Botón de Cerrar Sesión
        btnNavCerrarSesion = findViewById(R.id.btnNavCerrarSesion);


        // 3. Listener del botón de menú: Abre el Drawer
        btnMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        // 4. Listeners para los botones de "Agregar"
        btnAdd1.setOnClickListener(v ->
                Toast.makeText(this, "Producto 1 agregado al carrito", Toast.LENGTH_SHORT).show()
        );

        // ... (otros listeners de agregar productos se mantienen) ...

        // 5. Opciones del menú lateral (ejemplo)
        Button btnNavCarrito = findViewById(R.id.btnNavCarrito);
        if (btnNavCarrito != null) {
            btnNavCarrito.setOnClickListener(v -> {
                Toast.makeText(this, "Navegando al Carrito...", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        // ===============================================
        // LÓGICA DE CERRAR SESIÓN
        // ===============================================
        btnNavCerrarSesion.setOnClickListener(v -> {
            // Borrar el estado de la sesión
            SharedPreferences sharedPref = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(IS_LOGGED_IN, false); // Establecer como 'false'
            editor.apply();

            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();

            // Redirigir a MainActivity
            Intent intent = new Intent(Menu.this, MainActivity.class);
            // Flags para limpiar la pila de actividades y evitar volver atrás
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        // ===============================================


        // 6. SOLUCIÓN A LA ADVERTENCIA: OnBackPressedDispatcher
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // Si no está abierto el drawer, permitir que el botón Atrás funcione normalmente.
                    setEnabled(false);
                    Menu.super.onBackPressed();
                }
            }
        };

        // Registrar el callback con el dispatcher de la Activity
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}