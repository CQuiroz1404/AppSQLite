package com.example.appconsqlite.ui.main;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.appconsqlite.ui.product.AgregarProductoActivity;
import com.example.appconsqlite.ui.product.DetalleProductoActivity;
import com.example.appconsqlite.ui.profile.Perfil;
import com.example.appconsqlite.R;
import com.example.appconsqlite.ui.auth.MainActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;

import com.example.appconsqlite.data.repository.ProductRepository;
import com.example.appconsqlite.data.database.ProductContract;
import com.example.appconsqlite.utils.SessionManager;

public class Menu extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton btnMenu;
    private Button btnNavCuenta, btnNavAgregarProducto, btnNavMisProductos, btnNavCerrarSesion;

    private LinearLayout containerProductos;
    private ProductRepository productRepo;
    private SessionManager sessionManager;
    private long currentUserId;

    // Nuevos elementos para búsqueda y filtros
    private TextInputEditText etBuscarProducto;
    private ChipGroup chipGroupCategorias;
    private String categoriaActual = null; // null = todas las categorías
    private String busquedaActual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // FORZAR MODO CLARO
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);

        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_menu);

            // Configurar la barra de estado con color rojo
            getWindow().setStatusBarColor(getResources().getColor(R.color.red_primary, null));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.background_light_gray, null));

            // Inicializar SessionManager
            sessionManager = new SessionManager(this);

            // Verificar sesión y obtener userId
            if (!sessionManager.isLoggedIn()) {
                // Si no hay sesión, redirigir a login
                Intent intent = new Intent(Menu.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            currentUserId = sessionManager.getUserId();

            // Inicializar repositorio
            productRepo = new ProductRepository(this);

            // Inicializar Drawer y botones
            drawerLayout = findViewById(R.id.drawer_layout);
            btnMenu = findViewById(R.id.btnMenu);
            btnNavCuenta = findViewById(R.id.btnNavCuenta);
            btnNavAgregarProducto = findViewById(R.id.btnNavAgregarProducto);
            btnNavMisProductos = findViewById(R.id.btnNavMisProductos);
            btnNavCerrarSesion = findViewById(R.id.btnNavCerrarSesion);

            containerProductos = findViewById(R.id.containerProductos);

            // Nuevas vistas para búsqueda y filtros
            etBuscarProducto = findViewById(R.id.etBuscarProducto);
            chipGroupCategorias = findViewById(R.id.chipGroupCategorias);

            // Verificar que las vistas existen
            if (drawerLayout == null || btnMenu == null || containerProductos == null) {
                Toast.makeText(this, "Error al cargar la interfaz", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Configurar búsqueda en tiempo real
            configurarBusqueda();

            // Crear chips de categorías
            crearChipsCategorias();

            // Abrir Drawer
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

            // Mi Cuenta
            if (btnNavCuenta != null) {
                btnNavCuenta.setOnClickListener(v -> {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    startActivity(new Intent(Menu.this, Perfil.class));
                });
            }

            // Agregar Producto
            if (btnNavAgregarProducto != null) {
                btnNavAgregarProducto.setOnClickListener(v -> {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    Intent intent = new Intent(Menu.this, AgregarProductoActivity.class);
                    intent.putExtra("USER_ID", currentUserId);
                    startActivity(intent);
                });
            }

            // Mis Productos
            if (btnNavMisProductos != null) {
                btnNavMisProductos.setOnClickListener(v -> {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    cargarMisProductos();
                });
            }

            // Cerrar Sesión
            if (btnNavCerrarSesion != null) {
                btnNavCerrarSesion.setOnClickListener(v -> {
                    // Usar SessionManager para cerrar sesión de forma segura
                    sessionManager.logout();
                    Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(Menu.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }

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

        } catch (Exception e) {
            Toast.makeText(this, "Error al iniciar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    /**
     * Configura la búsqueda en tiempo real
     */
    private void configurarBusqueda() {
        etBuscarProducto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                busquedaActual = s.toString().trim();
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Crea los chips de categorías dinámicamente
     */
    private void crearChipsCategorias() {
        String[] categorias = ProductContract.Categories.getAllCategories();

        for (String categoria : categorias) {
            Chip chip = new Chip(this);
            chip.setText(categoria);
            chip.setCheckable(true);
            chip.setTextColor(getResources().getColor(android.R.color.white, null));
            chip.setChipBackgroundColorResource(R.color.chip_background_selector);
            chip.setTypeface(getResources().getFont(R.font.roland_variable_full));
            chip.setChipStrokeWidth(6f); // Ancho del borde
            chip.setChipStrokeColor(getResources().getColorStateList(R.color.chip_stroke_selector, null));

            chip.setOnClickListener(v -> {
                if (chip.isChecked()) {
                    categoriaActual = categoria;
                } else {
                    // Si se deselecciona, seleccionar "Todas"
                    categoriaActual = null;
                    Chip chipTodas = findViewById(R.id.chipTodas);
                    if (chipTodas != null) {
                        chipTodas.setChecked(true);
                    }
                }
                aplicarFiltros();
            });

            chipGroupCategorias.addView(chip);
        }

        // Configurar el chip "Todas"
        Chip chipTodas = findViewById(R.id.chipTodas);
        if (chipTodas != null) {
            chipTodas.setTypeface(getResources().getFont(R.font.roland_variable_full));
            chipTodas.setChipStrokeWidth(6f); // Ancho del borde
            chipTodas.setChipStrokeColor(getResources().getColorStateList(R.color.chip_stroke_selector, null));
            chipTodas.setOnClickListener(v -> {
                if (chipTodas.isChecked()) {
                    categoriaActual = null;
                    aplicarFiltros();
                }
            });
        }
    }

    /**
     * Aplica los filtros de búsqueda y categoría
     */
    private void aplicarFiltros() {
        containerProductos.removeAllViews();

        // Título dinámico
        TextView tvTitulo = new TextView(this);
        String titulo = "Marketplace";
        if (categoriaActual != null) {
            titulo += " - " + categoriaActual;
        }
        if (!busquedaActual.isEmpty()) {
            titulo = "Resultados: \"" + busquedaActual + "\"";
            if (categoriaActual != null) {
                titulo += " en " + categoriaActual;
            }
        }
        tvTitulo.setText(titulo);
        tvTitulo.setTextSize(20f);
        android.graphics.Typeface typeface = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.roland_variable_full);
        tvTitulo.setTypeface(typeface, android.graphics.Typeface.BOLD);
        tvTitulo.setPadding(16, 16, 16, 16);
        tvTitulo.setGravity(Gravity.CENTER);
        containerProductos.addView(tvTitulo);

        Cursor cursor = null;

        // Decidir qué consulta ejecutar
        if (!busquedaActual.isEmpty() && categoriaActual != null) {
            // Buscar por nombre Y categoría
            cursor = productRepo.buscarYFiltrarProductos(busquedaActual, categoriaActual);
        } else if (!busquedaActual.isEmpty()) {
            // Solo buscar por nombre
            cursor = productRepo.buscarProductosPorNombre(busquedaActual);
        } else if (categoriaActual != null) {
            // Solo filtrar por categoría
            cursor = productRepo.obtenerProductosPorCategoria(categoriaActual);
        } else {
            // Mostrar todos
            cursor = productRepo.obtenerTodosLosProductos();
        }

        if (cursor != null && cursor.moveToFirst()) {
            // Crear contenedor de productos en grid
            crearGridDeProductos(cursor, false);
            cursor.close();
        } else {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No se encontraron productos");
            tvVacio.setTextSize(16f);
            tvVacio.setGravity(Gravity.CENTER);
            tvVacio.setPadding(16, 32, 16, 16);
            containerProductos.addView(tvVacio);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            aplicarFiltros(); // Usar filtros en lugar de cargarProductos()
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarProductos() {
        aplicarFiltros(); // Delegar a aplicarFiltros
    }

    private void cargarMisProductos() {
        containerProductos.removeAllViews();

        // Título
        TextView tvTitulo = new TextView(this);
        tvTitulo.setText("Mis Productos");
        tvTitulo.setTextSize(20f);
        android.graphics.Typeface typeface = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.roland_variable_full);
        tvTitulo.setTypeface(typeface, android.graphics.Typeface.BOLD);
        tvTitulo.setPadding(16, 16, 16, 16);
        tvTitulo.setGravity(Gravity.CENTER);
        containerProductos.addView(tvTitulo);

        // Botón para volver a todos los productos
        Button btnVerTodos = new Button(this);
        btnVerTodos.setText("Ver Todos los Productos");
        btnVerTodos.setOnClickListener(v -> cargarProductos());
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.gravity = Gravity.CENTER;
        btnParams.setMargins(0, 0, 0, 16);
        btnVerTodos.setLayoutParams(btnParams);
        containerProductos.addView(btnVerTodos);

        Cursor cursor = productRepo.obtenerProductosPorUsuario(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            // Crear contenedor de productos en grid
            crearGridDeProductos(cursor, true);
            cursor.close();
        } else {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No has publicado ningún producto aún.");
            tvVacio.setTextSize(16f);
            tvVacio.setGravity(Gravity.CENTER);
            tvVacio.setPadding(16, 32, 16, 16);
            containerProductos.addView(tvVacio);
        }
    }

    // Método para crear grid de productos usando filas horizontales
    private void crearGridDeProductos(Cursor cursor, boolean mostrarBotones) {
        // Mover el cursor al inicio
        cursor.moveToPosition(-1);

        LinearLayout filaActual = null;
        int columnaActual = 0;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cardWidth = (screenWidth - 48) / 2; // 2 columnas con márgenes

        while (cursor.moveToNext()) {
            // Crear nueva fila cada 2 productos
            if (columnaActual == 0) {
                filaActual = new LinearLayout(this);
                filaActual.setOrientation(LinearLayout.HORIZONTAL);
                filaActual.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                filaActual.setWeightSum(2f);
                containerProductos.addView(filaActual);
            }

            // Crear la tarjeta del producto
            CardView card = crearTarjetaProducto(cursor, cardWidth, mostrarBotones);

            // Agregar la tarjeta a la fila actual
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );
            cardParams.setMargins(8, 8, 8, 8);
            card.setLayoutParams(cardParams);
            filaActual.addView(card);

            columnaActual++;

            // Si completamos 2 columnas, reiniciamos el contador
            if (columnaActual == 2) {
                columnaActual = 0;
            }
        }

        // Si quedó una fila incompleta, agregar un espacio vacío
        if (columnaActual == 1 && filaActual != null) {
            android.view.View espacioVacio = new android.view.View(this);
            LinearLayout.LayoutParams espacioParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );
            espacioParams.setMargins(8, 8, 8, 8);
            espacioVacio.setLayoutParams(espacioParams);
            filaActual.addView(espacioVacio);
        }
    }

    // Crear tarjeta individual de producto
    private CardView crearTarjetaProducto(Cursor cursor, int cardWidth, boolean mostrarBotones) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
        String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
        String descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"));
        double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"));

        // Obtener imagen de forma segura
        String imagenPath = "";
        try {
            int columnIndex = cursor.getColumnIndex("imagen");
            if (columnIndex != -1 && !cursor.isNull(columnIndex)) {
                imagenPath = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            imagenPath = "";
        }

        // Obtener user_id de forma segura
        long productUserId = -1;
        try {
            int columnIndex = cursor.getColumnIndex("usuario_id");
            if (columnIndex != -1 && !cursor.isNull(columnIndex)) {
                productUserId = cursor.getLong(columnIndex);
            }
        } catch (Exception e) {
            productUserId = -1;
        }

        // Verificar si es producto del usuario actual
        boolean esMiProducto = (productUserId == currentUserId);

        // Crear CardView para el producto
        CardView card = new CardView(this);
        card.setRadius(12f);
        card.setCardElevation(6f);
        card.setCardBackgroundColor(0xFFFFFFFF);

        // Click en la card para ver detalles
        card.setOnClickListener(v -> {
            Intent intent = new Intent(Menu.this, DetalleProductoActivity.class);
            intent.putExtra("PRODUCT_ID", id);
            startActivity(intent);
        });

        // Layout vertical (imagen arriba, info abajo)
        LinearLayout llMain = new LinearLayout(this);
        llMain.setOrientation(LinearLayout.VERTICAL);
        llMain.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Imagen del producto (cuadrada y grande)
        ImageView ivProducto = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                cardWidth // Imagen cuadrada
        );
        ivProducto.setLayoutParams(imgParams);
        ivProducto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivProducto.setBackgroundColor(0xFFF5F5F5);

        // Cargar imagen
        boolean imagenCargada = false;
        if (imagenPath != null && !imagenPath.isEmpty()) {
            try {
                File imgFile = new File(imagenPath);
                if (imgFile.exists()) {
                    ivProducto.setImageURI(Uri.fromFile(imgFile));
                    imagenCargada = true;
                } else {
                    Uri uri = Uri.parse(imagenPath);
                    ivProducto.setImageURI(uri);
                    imagenCargada = true;
                }
            } catch (Exception e) {
                // Usar imagen por defecto
            }
        }

        if (!imagenCargada) {
            ivProducto.setImageResource(android.R.drawable.ic_menu_gallery);
            android.graphics.drawable.Drawable drawable = ivProducto.getDrawable();
            if (drawable != null) {
                drawable.setTint(0xFFD32F2F);
            }
        }

        llMain.addView(ivProducto);

        // Container de información
        LinearLayout llInfo = new LinearLayout(this);
        llInfo.setOrientation(LinearLayout.VERTICAL);
        llInfo.setPadding(12, 12, 12, 12);
        llInfo.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Precio (más destacado, arriba)
        TextView tvPrecio = new TextView(this);
        tvPrecio.setText("$" + formatearPrecioChileno(precio));
        tvPrecio.setTextSize(20f);
        tvPrecio.setTextColor(0xFFD32F2F);
        android.graphics.Typeface typeface = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.roland_variable_full);
        tvPrecio.setTypeface(typeface, android.graphics.Typeface.BOLD);

        // Nombre del producto
        TextView tvNombre = new TextView(this);
        tvNombre.setText(nombre);
        tvNombre.setTextSize(14f);
        tvNombre.setTextColor(0xFF212121);
        tvNombre.setMaxLines(2);
        tvNombre.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tvNombre.setPadding(0, 6, 0, 0);

        llInfo.addView(tvPrecio);
        llInfo.addView(tvNombre);

        // Badge "Mi Producto" si es del usuario actual
        if (esMiProducto) {
            TextView tvBadge = new TextView(this);
            tvBadge.setText("Mío");
            tvBadge.setTextSize(10f);
            tvBadge.setTextColor(0xFFFFFFFF);
            tvBadge.setPadding(8, 4, 8, 4);
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            badgeParams.setMargins(0, 6, 0, 0);
            tvBadge.setLayoutParams(badgeParams);

            android.graphics.drawable.GradientDrawable badgeShape = new android.graphics.drawable.GradientDrawable();
            badgeShape.setColor(0xFFD32F2F);
            badgeShape.setCornerRadius(12f);
            tvBadge.setBackground(badgeShape);

            llInfo.addView(tvBadge);
        }

        llMain.addView(llInfo);
        card.addView(llMain);

        return card;
    }

    // Método para formatear precios en pesos chilenos con separadores de miles
    private String formatearPrecioChileno(double precio) {
        java.text.DecimalFormat formato = new java.text.DecimalFormat("#,###");
        return formato.format(precio);
    }

    private void confirmarEliminar(long productId, String nombreProducto) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Deseas eliminar '" + nombreProducto + "'? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    boolean exito = productRepo.eliminarProducto(productId);
                    if (exito) {
                        Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                        cargarMisProductos();
                    } else {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
