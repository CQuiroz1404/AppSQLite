package com.example.appconsqlite;

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

    // Variables no usadas, pero se mantienen si son necesarias para otra lógica
    int numero;
    int num1;
    int num3;
    int num2;

    // Declaración de botones
    private Button btnAdd1, btnAdd2, btnAdd3, btnAdd4;
    private ImageButton btnMenu; // Botón para el menú/aside

    // Declaración del DrawerLayout
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        // ==========================================================
        // SE ELIMINA el setOnApplyWindowInsetsListener
        // La corrección de posición se hace en el XML con fitsSystemWindows="true"
        // ==========================================================

        // 1. Inicializar el DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // 2. Inicializar botones de la vista
        btnAdd1 = findViewById(R.id.btnAdd1);
        btnAdd2 = findViewById(R.id.btnAdd2);
        btnAdd3 = findViewById(R.id.btnAdd3);
        btnAdd4 = findViewById(R.id.btnAdd4);
        btnMenu = findViewById(R.id.btnMenu);

        // 3. Listener del botón de menú: Abre el Drawer
        btnMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        // 4. Listeners para los botones de "Agregar"
        btnAdd1.setOnClickListener(v ->
                Toast.makeText(this, "Producto 1 agregado al carrito", Toast.LENGTH_SHORT).show()
        );

        btnAdd2.setOnClickListener(v ->
                Toast.makeText(this, "Producto 2 agregado al carrito", Toast.LENGTH_SHORT).show()
        );

        btnAdd3.setOnClickListener(v ->
                Toast.makeText(this, "Producto 3 agregado al carrito", Toast.LENGTH_SHORT).show()
        );

        btnAdd4.setOnClickListener(v ->
                Toast.makeText(this, "Producto 4 agregado al carrito", Toast.LENGTH_SHORT).show()
        );

        // 5. Opciones del menú lateral (ejemplo)
        // Usamos IDs de los botones que definiste en el XML del menú lateral.
        Button btnNavCarrito = findViewById(R.id.btnNavCarrito);
        if (btnNavCarrito != null) {
            btnNavCarrito.setOnClickListener(v -> {
                Toast.makeText(this, "Navegando al Carrito...", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        // 6. SOLUCIÓN A LA ADVERTENCIA: OnBackPressedDispatcher
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

        // Registrar el callback con el dispatcher de la Activity
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}