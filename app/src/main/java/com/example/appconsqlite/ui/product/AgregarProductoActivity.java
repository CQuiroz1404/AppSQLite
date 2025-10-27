package com.example.appconsqlite.ui.product;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appconsqlite.R;
import com.example.appconsqlite.data.repository.ProductRepository;
import com.example.appconsqlite.data.database.ProductContract;
import com.example.appconsqlite.utils.PermissionHelper;
import com.example.appconsqlite.utils.SessionManager;
import com.example.appconsqlite.utils.ImageHelper;

public class AgregarProductoActivity extends AppCompatActivity {

    EditText etNombre, etDescripcion, etPrecio;
    ImageView ivImagen;
    ImageButton btnVolverAgregarProducto;
    Button btnSubir, btnSeleccionarImagen, btnDisminuirCantidad, btnAumentarCantidad, btnSeleccionarCategoria;
    TextView tvCantidadProducto, tvCategoriaSeleccionada;

    Uri imagenUri = null;
    ProductRepository productRepo;
    SessionManager sessionManager;
    long userId;
    private int cantidad = 1;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private String categoriaSeleccionada = ProductContract.Categories.OTROS;

    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_producto);

        // Configurar la barra de estado con color rojo
        getWindow().setStatusBarColor(getResources().getColor(R.color.red_primary, null));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background_light_gray, null));

        etNombre = findViewById(R.id.etNombreProducto);
        etDescripcion = findViewById(R.id.etDescripcionProducto);
        etPrecio = findViewById(R.id.etPrecioProducto);
        ivImagen = findViewById(R.id.ivImagenProducto);
        btnVolverAgregarProducto = findViewById(R.id.btnVolverAgregarProducto);
        btnSubir = findViewById(R.id.btnSubirProducto);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnDisminuirCantidad = findViewById(R.id.btnDisminuirCantidad);
        btnAumentarCantidad = findViewById(R.id.btnAumentarCantidad);
        tvCantidadProducto = findViewById(R.id.tvCantidadProducto);
        btnSeleccionarCategoria = findViewById(R.id.btnSeleccionarCategoria);
        tvCategoriaSeleccionada = findViewById(R.id.tvCategoriaSeleccionada);

        productRepo = new ProductRepository(this);
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        if (userId == -1) {
            Toast.makeText(this, "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnVolverAgregarProducto.setOnClickListener(v -> finish());

        tvCategoriaSeleccionada.setText(categoriaSeleccionada);

        // Maneja solicitud de permisos de galería
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                    boolean readExternal = permissions.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false);
                    boolean readMediaImages = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        readMediaImages = permissions.getOrDefault(Manifest.permission.READ_MEDIA_IMAGES, false);
                    }
                    boolean storageGranted = readExternal || readMediaImages;

                    if (storageGranted) {
                        abrirGaleria();
                    } else {
                        Toast.makeText(this, "Permiso de galería denegado", Toast.LENGTH_LONG).show();
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imagenUri = result.getData().getData();
                        if (imagenUri != null) {
                            ivImagen.setImageURI(imagenUri);
                        }
                    }
                });

        btnSeleccionarImagen.setOnClickListener(v -> checkGalleryPermissionAndOpen());
        btnSubir.setOnClickListener(v -> subirProducto());
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

        new AlertDialog.Builder(this)
                .setTitle("Seleccionar Categoría")
                .setItems(categorias, (dialog, which) -> {
                    categoriaSeleccionada = categorias[which];
                    tvCategoriaSeleccionada.setText(categoriaSeleccionada);
                })
                .show();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void checkGalleryPermissionAndOpen() {
        if (!PermissionHelper.hasStoragePermission(this)) {
            requestPermissionLauncher.launch(PermissionHelper.getGalleryPermissions());
        } else {
            abrirGaleria();
        }
    }

    private void subirProducto() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        // Validar campos obligatorios (nombre y precio)
        if (nombre.isEmpty() || precioStr.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios (Nombre y Precio)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que el precio sea un número válido y mayor a 0
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

        String imagePath = "";
        if (imagenUri != null) {
            Toast.makeText(this, "Guardando imagen...", Toast.LENGTH_SHORT).show();
            imagePath = ImageHelper.saveImageToInternalStorage(this, imagenUri, nombre);

            if (imagePath == null) {
                Toast.makeText(this, "Advertencia: No se pudo guardar la imagen", Toast.LENGTH_SHORT).show();
                imagePath = "";
            }
        }

        boolean exito = productRepo.insertarProducto(nombre, descripcion, precio, userId, imagePath, cantidad, categoriaSeleccionada);

        if (exito) {
            Toast.makeText(this, "Producto publicado en el marketplace", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!imagePath.isEmpty()) {
                ImageHelper.deleteImage(imagePath);
            }
            Toast.makeText(this, "Error al publicar el producto", Toast.LENGTH_SHORT).show();
        }
    }
}
