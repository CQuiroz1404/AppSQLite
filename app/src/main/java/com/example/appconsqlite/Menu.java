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
    private Button btnAdd1, btnAdd2, btnAdd3, btnAdd4, btnNavCerrarSesion;
    private ImageButton btnMenu; // Botón para el menú/aside

    // Declaración de botones del menú lateral
    private Button btnNavCarrito;
    private Button btnNavCuenta; // ¡AÑADIDO!
    private Button btnNavPedidos;

    // Declaración del DrawerLayout
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        // 1. Inicializar el DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // 2. Inicializar botones de la vista principal
        btnAdd1 = findViewById(R.id.btnAdd1);
        btnAdd2 = findViewById(R.id.btnAdd2);
        btnAdd3 = findViewById(R.id.btnAdd3);
        btnAdd4 = findViewById(R.id.btnAdd4);
        btnMenu = findViewById(R.id.btnMenu);

        // 3. Inicializar botones del menú lateral
        btnNavCarrito = findViewById(R.id.btnNavCarrito);
        btnNavCuenta = findViewById(R.id.btnNavCuenta); // ¡INICIALIZADO!
        btnNavPedidos = findViewById(R.id.btnNavPedidos);
        btnNavCerrarSesion = findViewById(R.id.btnNavCerrarSesion);


        // 4. Listener del botón de menú: Abre el Drawer
        btnMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        // 5. Listeners para los botones de "Agregar"
        btnAdd1.setOnClickListener(v ->
                Toast.makeText(this, "Producto 1 agregado al carrito", Toast.LENGTH_SHORT).show()
        );
        // ... (otros listeners de agregar productos se mantienen) ...
        btnAdd2.setOnClickListener(v ->
                Toast.makeText(this, "Producto 2 agregado al carrito", Toast.LENGTH_SHORT).show()
        );
        btnAdd3.setOnClickListener(v ->
                Toast.makeText(this, "Producto 3 agregado al carrito", Toast.LENGTH_SHORT).show()
        );
        btnAdd4.setOnClickListener(v ->
                Toast.makeText(this, "Producto 4 agregado al carrito", Toast.LENGTH_SHORT).show()
        );

        // 6. Opciones del menú lateral

        // Listener para Ver Carrito
        if (btnNavCarrito != null) {
            btnNavCarrito.setOnClickListener(v -> {
                Toast.makeText(this, "Navegando al Carrito...", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        // ===============================================
        // LÓGICA DE NAVEGACIÓN A PERFIL (MI CUENTA)
        // ===============================================
        if (btnNavCuenta != null) {
            btnNavCuenta.setOnClickListener(v -> {
                Toast.makeText(this, "Abriendo Mi Cuenta...", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);

                // Abrir la actividad Perfil
                Intent intent = new Intent(Menu.this, Perfil.class);
                startActivity(intent);
            });
        }
        // ===============================================

        // 7. LÓGICA DE CERRAR SESIÓN (Se mantiene)
        btnNavCerrarSesion.setOnClickListener(v -> {
            SharedPreferences sharedPref = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(IS_LOGGED_IN, false);
            editor.apply();

            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Menu.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });


        // 8. OnBackPressedDispatcher (Se mantiene)
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    Menu.super.onBackPressed();
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}