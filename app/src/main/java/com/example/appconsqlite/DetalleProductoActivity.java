package com.example.appconsqlite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.File;

public class DetalleProductoActivity extends AppCompatActivity {

    private static final String PREFS_FILE = "com.example.appconsqlite.PREFERENCE_FILE_KEY";
    private static final String USER_ID = "userId";

    private ImageView ivProductoDetalle;
    private TextView tvNombreDetalle, tvDescripcionDetalle, tvPrecioDetalle, tvVendedorInfo;
    private Button btnEditar, btnEliminar, btnVolver;

    private ProductRepository productRepo;
    private long productId;
    private long currentUserId;
    private boolean esMiProducto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_producto);

        // Inicializar vistas
        ivProductoDetalle = findViewById(R.id.ivProductoDetalle);
        tvNombreDetalle = findViewById(R.id.tvNombreDetalle);
        tvDescripcionDetalle = findViewById(R.id.tvDescripcionDetalle);
        tvPrecioDetalle = findViewById(R.id.tvPrecioDetalle);
        tvVendedorInfo = findViewById(R.id.tvVendedorInfo);
        btnEditar = findViewById(R.id.btnEditarDetalle);
        btnEliminar = findViewById(R.id.btnEliminarDetalle);
        btnVolver = findViewById(R.id.btnVolverDetalle);

        productRepo = new ProductRepository(this);

        // Obtener IDs
        SharedPreferences sharedPref = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        currentUserId = sharedPref.getLong(USER_ID, -1);
        productId = getIntent().getLongExtra("PRODUCT_ID", -1);

        if (productId == -1) {
            Toast.makeText(this, "Error: Producto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cargar datos del producto
        cargarProducto();

        // Configurar botones
        btnVolver.setOnClickListener(v -> finish());

        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(DetalleProductoActivity.this, EditarProductoActivity.class);
            intent.putExtra("PRODUCT_ID", productId);
            startActivity(intent);
            finish();
        });

        btnEliminar.setOnClickListener(v -> mostrarDialogoEliminar());
    }

    private void cargarProducto() {
        Cursor cursor = productRepo.obtenerProductoPorId(productId);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                String descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"));
                double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"));
                String imagenPath = cursor.getString(cursor.getColumnIndexOrThrow("imagen"));
                long productUserId = cursor.getLong(cursor.getColumnIndexOrThrow("usuario_id"));

                // Verificar si es mi producto
                esMiProducto = (productUserId == currentUserId);

                // Mostrar datos
                tvNombreDetalle.setText(nombre);
                tvDescripcionDetalle.setText(descripcion != null && !descripcion.isEmpty() ?
                    descripcion : "Sin descripción disponible");
                tvPrecioDetalle.setText("$" + formatearPrecioChileno(precio));

                // Mostrar info del vendedor
                if (esMiProducto) {
                    tvVendedorInfo.setText("Este es tu producto");
                    btnEditar.setVisibility(android.view.View.VISIBLE);
                    btnEliminar.setVisibility(android.view.View.VISIBLE);
                } else {
                    tvVendedorInfo.setText("Producto de otro vendedor");
                    btnEditar.setVisibility(android.view.View.GONE);
                    btnEliminar.setVisibility(android.view.View.GONE);
                }

                // Cargar imagen desde el almacenamiento interno
                if (imagenPath != null && !imagenPath.isEmpty()) {
                    File imgFile = new File(imagenPath);
                    if (imgFile.exists()) {
                        ivProductoDetalle.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
                    } else {
                        ivProductoDetalle.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } else {
                    ivProductoDetalle.setImageResource(android.R.drawable.ic_menu_gallery);
                }

            } finally {
                cursor.close();
            }
        } else {
            if (cursor != null) {
                cursor.close();
            }
            Toast.makeText(this, "Error al cargar el producto", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Método para formatear precios en pesos chilenos con separadores de miles
    private String formatearPrecioChileno(double precio) {
        java.text.DecimalFormat formato = new java.text.DecimalFormat("#,###");
        return formato.format(precio);
    }

    private void mostrarDialogoEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Estás seguro de que deseas eliminar este producto? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Obtener la ruta de la imagen antes de eliminar el producto
                    Cursor cursor = productRepo.obtenerProductoPorId(productId);
                    String imagePath = null;
                    if (cursor != null && cursor.moveToFirst()) {
                        imagePath = cursor.getString(cursor.getColumnIndexOrThrow("imagen"));
                        cursor.close();
                    }

                    // Eliminar el producto de la base de datos
                    boolean exito = productRepo.eliminarProducto(productId);
                    if (exito) {
                        // Eliminar también la imagen del almacenamiento
                        if (imagePath != null && !imagePath.isEmpty()) {
                            ImageHelper.deleteImage(imagePath);
                        }
                        Toast.makeText(this, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al eliminar producto", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarProducto();
    }
}
