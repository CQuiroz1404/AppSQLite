package com.example.appconsqlite;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class EditarProductoActivity extends AppCompatActivity {

    EditText etNombre, etDescripcion, etPrecio;
    ImageView ivImagen;
    Button btnActualizar, btnSeleccionarImagen, btnEliminar, btnDisminuirCantidad, btnAumentarCantidad, btnSeleccionarCategoria;
    TextView tvCantidadProducto, tvCategoriaSeleccionada;
    private int cantidad = 1;
    private String categoriaSeleccionada = ProductContract.Categories.OTROS;

    Uri imagenUri = null;
    String imagenPath = "";
    ProductRepository productRepo;
    SessionManager sessionManager;
    long userId;
    long productId;

    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_producto);

        // Configurar la barra de estado con color rojo
        getWindow().setStatusBarColor(getResources().getColor(R.color.red_primary, null));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background_light_gray, null));

        etNombre = findViewById(R.id.etNombreProducto);
        etDescripcion = findViewById(R.id.etDescripcionProducto);
        etPrecio = findViewById(R.id.etPrecioProducto);
        ivImagen = findViewById(R.id.ivImagenProducto);
        btnActualizar = findViewById(R.id.btnActualizarProducto);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnEliminar = findViewById(R.id.btnEliminarProducto);
        btnDisminuirCantidad = findViewById(R.id.btnDisminuirCantidad);
        btnAumentarCantidad = findViewById(R.id.btnAumentarCantidad);
        tvCantidadProducto = findViewById(R.id.tvCantidadProducto);
        btnSeleccionarCategoria = findViewById(R.id.btnSeleccionarCategoria);
        tvCategoriaSeleccionada = findViewById(R.id.tvCategoriaSeleccionada);

        productRepo = new ProductRepository(this);

        // Obtener userId desde SessionManager
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        productId = getIntent().getLongExtra("PRODUCT_ID", -1);

        if (productId == -1) {
            Toast.makeText(this, "Error: Producto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!productRepo.esProductoDelUsuario(productId, userId)) {
            Toast.makeText(this, "No tienes permiso para editar este producto", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imagenUri = result.getData().getData();
                        if (imagenUri != null) {
                            ivImagen.setImageURI(imagenUri);
                        }
                    }
                });

        cargarDatosProducto();

        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        btnActualizar.setOnClickListener(v -> actualizarProducto());
        btnEliminar.setOnClickListener(v -> eliminarProducto());
        btnSeleccionarCategoria.setOnClickListener(v -> mostrarSelectorCategoria());

        btnAumentarCantidad.setOnClickListener(v -> {
            cantidad++;
            tvCantidadProducto.setText(String.valueOf(cantidad));
        });

        btnDisminuirCantidad.setOnClickListener(v -> {
            if (cantidad > 1) {
                cantidad--;
                tvCantidadProducto.setText(String.valueOf(cantidad));
            }
        });
    }

    private void mostrarSelectorCategoria() {
        String[] categorias = ProductContract.Categories.getAllCategories();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Categoría");
        builder.setItems(categorias, (dialog, which) -> {
            categoriaSeleccionada = categorias[which];
            tvCategoriaSeleccionada.setText(categoriaSeleccionada);
        });
        builder.show();
    }

    private void cargarDatosProducto() {
        Cursor cursor = productRepo.obtenerProductoPorId(productId);
        if (cursor != null && cursor.moveToFirst()) {
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_NAME));
            String descripcion = cursor.getString(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_DESC));
            double precio = cursor.getDouble(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_PRICE));
            imagenPath = cursor.getString(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_IMAGE_PATH));
            cantidad = cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_QUANTITY));

            // Cargar categoría
            int categoriaIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_CATEGORY);
            if (categoriaIndex != -1 && !cursor.isNull(categoriaIndex)) {
                categoriaSeleccionada = cursor.getString(categoriaIndex);
            } else {
                categoriaSeleccionada = ProductContract.Categories.OTROS;
            }


            etNombre.setText(nombre);
            etDescripcion.setText(descripcion);
            etPrecio.setText(String.valueOf(precio));
            tvCantidadProducto.setText(String.valueOf(cantidad));
            tvCategoriaSeleccionada.setText(categoriaSeleccionada);

            if (imagenPath != null && !imagenPath.isEmpty()) {
                File imgFile = new File(imagenPath);
                if (imgFile.exists()) {
                    ivImagen.setImageURI(Uri.fromFile(imgFile));
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
            if (precio <= 0) {
                Toast.makeText(this, "El precio debe ser mayor a $0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        String newImagePath = imagenPath;
        if (imagenUri != null) {
            newImagePath = ImageHelper.saveImageToInternalStorage(this, imagenUri, nombre);
            if (newImagePath != null) {
                if (imagenPath != null && !imagenPath.isEmpty() && !imagenPath.equals(newImagePath)) {
                    ImageHelper.deleteImage(imagenPath);
                }
            } else {
                Toast.makeText(this, "Advertencia: No se pudo actualizar la imagen", Toast.LENGTH_SHORT).show();
                newImagePath = imagenPath;
            }
        }

        boolean exito = productRepo.actualizarProducto(productId, nombre, descripcion, precio, newImagePath, cantidad, categoriaSeleccionada);

        if (exito) {
            Toast.makeText(this, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al actualizar producto", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarProducto() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Estás seguro de que deseas eliminar este producto?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    boolean exito = productRepo.eliminarProducto(productId);
                    if (exito) {
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
