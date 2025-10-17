package com.example.appconsqlite;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AgregarProductoActivity extends AppCompatActivity {
    private static final String PREFS_FILE = "com.example.appconsqlite.PREFERENCE_FILE_KEY";
    private static final String USER_ID = "userId";

    EditText etNombre, etDescripcion, etPrecio;
    ImageView ivImagen;
    Button btnSubir, btnSeleccionarImagen, btnDisminuirCantidad, btnAumentarCantidad;
    TextView tvCantidadProducto;

    Uri imagenUri = null;
    ProductRepository productRepo;
    long userId;
    private int cantidad = 1; // Variable para almacenar la cantidad

    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_producto);

        // Inicialización de vistas existentes
        etNombre = findViewById(R.id.etNombreProducto);
        etDescripcion = findViewById(R.id.etDescripcionProducto);
        etPrecio = findViewById(R.id.etPrecioProducto);
        ivImagen = findViewById(R.id.ivImagenProducto);
        btnSubir = findViewById(R.id.btnSubirProducto);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);


        // Inicialización de nuevas vistas para la cantidad
        btnDisminuirCantidad = findViewById(R.id.btnDisminuirCantidad);
        btnAumentarCantidad = findViewById(R.id.btnAumentarCantidad);
        tvCantidadProducto = findViewById(R.id.tvCantidadProducto);


        productRepo = new ProductRepository(this);

        // Obtener el userId desde SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        userId = sharedPref.getLong(USER_ID, -1);

        if (userId == -1) {
            Toast.makeText(this, "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar launcher de galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imagenUri = result.getData().getData();
                        if (imagenUri != null) {
                            ivImagen.setImageURI(imagenUri);
                        }
                    }
                });

        // Configuración de listeners de clics
        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        btnSubir.setOnClickListener(v -> subirProducto());


        // Listeners para los botones de cantidad
        btnAumentarCantidad.setOnClickListener(v -> {
            cantidad++;
            tvCantidadProducto.setText(String.valueOf(cantidad));
        });

        btnDisminuirCantidad.setOnClickListener(v -> {
            if (cantidad > 1) { // Evitar que la cantidad sea menor que 1
                cantidad--;
                tvCantidadProducto.setText(String.valueOf(cantidad));
            }
        });

    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void subirProducto() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (nombre.isEmpty() || precioStr.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios (Nombre y Precio)", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
            if (precio <= 0) {
                Toast.makeText(this, "El precio debe ser mayor a $0 (pesos chilenos)", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Precio inválido. Ingrese solo números (sin puntos ni comas)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar la imagen de forma permanente usando ImageHelper
        String imagePath = "";
        if (imagenUri != null) {
            Toast.makeText(this, "Guardando imagen...", Toast.LENGTH_SHORT).show();
            imagePath = ImageHelper.saveImageToInternalStorage(this, imagenUri, nombre);

            if (imagePath == null) {
                Toast.makeText(this, "Advertencia: No se pudo guardar la imagen", Toast.LENGTH_SHORT).show();
                imagePath = ""; // Continuar sin imagen
            }
        }


        // Ahora usamos la variable 'cantidad' al insertar el producto.
        boolean exito = productRepo.insertarProducto(nombre, descripcion, precio, userId, imagePath, cantidad);

        if (exito) {
            Toast.makeText(this, "Producto publicado en el marketplace", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Si falló, eliminar la imagen que se guardó
            if (!imagePath.isEmpty()) {
                ImageHelper.deleteImage(imagePath);
            }
            Toast.makeText(this, "Error al publicar producto", Toast.LENGTH_SHORT).show();
        }
    }
}
