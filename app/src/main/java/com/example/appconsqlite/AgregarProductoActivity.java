package com.example.appconsqlite;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class AgregarProductoActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    EditText etNombre, etDescripcion, etPrecio;
    ImageView ivImagen;
    Button btnSubir, btnSeleccionarImagen;

    Uri imagenUri = null;
    ProductRepository productRepo;
    long userId; // Recibido del login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_producto);

        etNombre = findViewById(R.id.etNombreProducto);
        etDescripcion = findViewById(R.id.etDescripcionProducto);
        etPrecio = findViewById(R.id.etPrecioProducto);
        ivImagen = findViewById(R.id.ivImagenProducto);
        btnSubir = findViewById(R.id.btnSubirProducto);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);

        productRepo = new ProductRepository(this);

        // Recibe el ID del usuario actual
        userId = getIntent().getLongExtra("USER_ID", -1);

        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        btnSubir.setOnClickListener(v -> subirProducto());
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imagenUri = data.getData();
            ivImagen.setImageURI(imagenUri);
        }
    }

    private void subirProducto() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (nombre.isEmpty() || precioStr.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio = Double.parseDouble(precioStr);
        String imagePath = imagenUri != null ? imagenUri.toString() : null;

        boolean exito = productRepo.insertarProducto(nombre, descripcion, precio, userId, imagePath);

        if (exito) {
            Toast.makeText(this, "Producto agregado correctamente", Toast.LENGTH_SHORT).show();
            finish(); // volver al menú
        } else {
            Toast.makeText(this, "Error al guardar producto", Toast.LENGTH_SHORT).show();
        }
    }
}
