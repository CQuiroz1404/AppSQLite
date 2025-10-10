package com.example.appconsqlite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.File;

public class EditarProductoActivity extends AppCompatActivity {
    private static final String PREFS_FILE = "com.example.appconsqlite.PREFERENCE_FILE_KEY";
    private static final String USER_ID = "userId";

    EditText etNombre, etDescripcion, etPrecio;
    ImageView ivImagen;
    Button btnActualizar, btnSeleccionarImagen, btnEliminar;

    Uri imagenUri = null;
    String imagenPath = "";
    ProductRepository productRepo;
    long userId;
    long productId;

    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_producto);

        etNombre = findViewById(R.id.etNombreProducto);
        etDescripcion = findViewById(R.id.etDescripcionProducto);
        etPrecio = findViewById(R.id.etPrecioProducto);
        ivImagen = findViewById(R.id.ivImagenProducto);
        btnActualizar = findViewById(R.id.btnActualizarProducto);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnEliminar = findViewById(R.id.btnEliminarProducto);

        productRepo = new ProductRepository(this);

        // Obtener userId de SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        userId = sharedPref.getLong(USER_ID, -1);

        // Obtener productId del Intent
        productId = getIntent().getLongExtra("PRODUCT_ID", -1);

        if (productId == -1) {
            Toast.makeText(this, "Error: Producto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Verificar que el usuario sea dueño del producto
        if (!productRepo.esProductoDelUsuario(productId, userId)) {
            Toast.makeText(this, "No tienes permiso para editar este producto", Toast.LENGTH_LONG).show();
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
                            imagenPath = imagenUri.toString();
                        }
                    }
                });

        // Cargar datos del producto
        cargarDatosProducto();

        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        btnActualizar.setOnClickListener(v -> actualizarProducto());
        btnEliminar.setOnClickListener(v -> eliminarProducto());
    }

    private void cargarDatosProducto() {
        Cursor cursor = productRepo.obtenerProductoPorId(productId);
        if (cursor != null && cursor.moveToFirst()) {
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
            String descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"));
            double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"));
            imagenPath = cursor.getString(cursor.getColumnIndexOrThrow("imagen"));

            etNombre.setText(nombre);
            etDescripcion.setText(descripcion);
            etPrecio.setText(String.valueOf(precio));

            // Cargar imagen si existe
            if (imagenPath != null && !imagenPath.isEmpty()) {
                File imgFile = new File(imagenPath);
                if (imgFile.exists()) {
                    ivImagen.setImageURI(Uri.fromFile(imgFile));
                } else {
                    // Si es una URI
                    ivImagen.setImageURI(Uri.parse(imagenPath));
                }
            }

            cursor.close();
        } else {
            Toast.makeText(this, "Error al cargar producto", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void actualizarProducto() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (nombre.isEmpty() || precioStr.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si se seleccionó una nueva imagen, guardarla
        String newImagePath = imagenPath; // Mantener la imagen actual por defecto
        if (imagenUri != null) {
            // Guardar la nueva imagen
            newImagePath = ImageHelper.saveImageToInternalStorage(this, imagenUri, nombre);

            if (newImagePath != null) {
                // Si se guardó exitosamente y había una imagen anterior, eliminarla
                if (imagenPath != null && !imagenPath.isEmpty() && !imagenPath.equals(newImagePath)) {
                    ImageHelper.deleteImage(imagenPath);
                }
            } else {
                // Si falló al guardar la nueva imagen, mantener la anterior
                Toast.makeText(this, "Advertencia: No se pudo actualizar la imagen", Toast.LENGTH_SHORT).show();
                newImagePath = imagenPath;
            }
        }

        boolean exito = productRepo.actualizarProducto(productId, nombre, descripcion, precio, newImagePath);

        if (exito) {
            Toast.makeText(this, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al actualizar producto", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarProducto() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Estás seguro de que deseas eliminar este producto?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    boolean exito = productRepo.eliminarProducto(productId);
                    if (exito) {
                        // Eliminar también la imagen del almacenamiento
                        if (imagenPath != null && !imagenPath.isEmpty()) {
                            ImageHelper.deleteImage(imagenPath);
                        }
                        Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al eliminar producto", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
