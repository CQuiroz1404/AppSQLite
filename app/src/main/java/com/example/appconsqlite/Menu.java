package com.example.appconsqlite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class Menu extends AppCompatActivity {

    private static final String PREFS_FILE = "com.example.appconsqlite.PREFERENCE_FILE_KEY";
    private static final String IS_LOGGED_IN = "isLoggedIn";

    private DrawerLayout drawerLayout;
    private ImageButton btnMenu;
    private Button btnNavCuenta, btnNavAgregarProducto, btnNavCerrarSesion;

    private LinearLayout containerProductos;
    private ProductRepository productRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        // Inicializar repositorio
        productRepo = new ProductRepository(this);

        // Inicializar Drawer y botones
        drawerLayout = findViewById(R.id.drawer_layout);
        btnMenu = findViewById(R.id.btnMenu);
        btnNavCuenta = findViewById(R.id.btnNavCuenta);
        btnNavAgregarProducto = findViewById(R.id.btnNavAgregarProducto);
        btnNavCerrarSesion = findViewById(R.id.btnNavCerrarSesion);

        containerProductos = findViewById(R.id.containerProductos);

        // Abrir Drawer
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Mi Cuenta
        btnNavCuenta.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(Menu.this, Perfil.class));
        });

        // Agregar Producto
        btnNavAgregarProducto.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(Menu.this, AgregarProductoActivity.class));
        });

        // Cerrar Sesión
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

        // Manejo del botón Back
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
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

        // Cargar productos
        cargarProductos();
    }

    private void cargarProductos() {
        containerProductos.removeAllViews();

        Cursor cursor = productRepo.obtenerTodosLosProductos();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                agregarProductoCard(cursor);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No hay productos disponibles.");
            tvVacio.setTextSize(18f);
            tvVacio.setGravity(Gravity.CENTER);
            tvVacio.setPadding(16,16,16,16);
            containerProductos.addView(tvVacio);
        }
    }

    private void agregarProductoCard(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
        String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
        String descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"));
        double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"));

        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(8,8,8,8);
        card.setLayoutParams(cardParams);
        card.setRadius(12f);
        card.setCardElevation(6f);

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(16,16,16,16);

        TextView tvNombre = new TextView(this);
        tvNombre.setText(nombre);
        tvNombre.setTextSize(18f);
        tvNombre.setTypeface(ResourcesCompat.getFont(this, R.font.roland_variable_full));

        TextView tvDesc = new TextView(this);
        tvDesc.setText(descripcion);
        tvDesc.setPadding(0,8,0,0);
        tvDesc.setTypeface(ResourcesCompat.getFont(this, R.font.roland_variable_full));

        TextView tvPrecio = new TextView(this);
        tvPrecio.setText("Precio: $" + precio);
        tvPrecio.setPadding(0,8,0,0);
        tvPrecio.setTypeface(ResourcesCompat.getFont(this, R.font.roland_variable_full));

        ll.addView(tvNombre);
        ll.addView(tvDesc);
        ll.addView(tvPrecio);

        card.addView(ll);
        containerProductos.addView(card);
    }
}
