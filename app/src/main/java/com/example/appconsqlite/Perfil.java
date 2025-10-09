package com.example.appconsqlite;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import de.hdodenhof.circleimageview.CircleImageView;

import android.graphics.BitmapFactory; // NUEVA: Para cargar imágenes desde rutas de archivo
import java.io.File; // NUEVA: Para verificar si la ruta es un archivo

public class Perfil extends AppCompatActivity {

    // Vistas principales para mostrar datos
    private CircleImageView ivProfilePicture;
    private TextView tvUserName;

    // Vistas para la información de contacto (NUEVAS)
    private TextView tvUserEmail;
    private TextView tvUserPhone;
    private TextView tvUserAddress;

    // Constantes de SharedPreferences (deben coincidir con MainActivity)
    private static final String PREFS_FILE = "com.example.appconsqlite.PREFERENCE_FILE_KEY";
    private static final String USER_ID = "userId";

    private UserRepository userRepo;

    // Vistas del menú de opciones
    private LinearLayout llAjustes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar repositorio
        userRepo = new UserRepository(this);

        // 1. Inicializar las vistas de datos
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvUserAddress = findViewById(R.id.tvUserAddress);

        // 2. Inicializar los contenedores de las opciones de menú
        llAjustes = findViewById(R.id.llAjustes);

        setupMenuListeners();

        // 3. Cargar datos del usuario
        loadUserData();
    }

    /**
     * Carga los datos del usuario logeado desde la base de datos.
     */
    private void loadUserData() {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        // Obtener el ID del usuario. -1 es el valor por defecto si no se encuentra.
        long userId = sharedPref.getLong(USER_ID, -1);

        if (userId != -1) {
            Cursor cursor = userRepo.obtenerDatosPerfil(userId);

            if (cursor != null && cursor.moveToFirst()) {
                try {
                    // Obtener los datos del cursor
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_NOMBRE));
                    String apellido = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_APELLIDO));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_EMAIL));
                    String telefono = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_TELEFONO));
                    String direccion = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_DIRECCION));
                    String fotoPath = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_FOTO_PERFIL_PATH));

                    // Establecer el Nombre Completo
                    String fullName = (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
                    tvUserName.setText(fullName.trim().isEmpty() ? "Nombre y Apellido" : fullName.trim());

                    // Establecer Email
                    tvUserEmail.setText(email);

                    // Establecer Teléfono y Dirección
                    tvUserPhone.setText(telefono != null && !telefono.isEmpty() ? telefono : "Teléfono: N/A");
                    tvUserAddress.setText(direccion != null && !direccion.isEmpty() ? direccion : "Dirección: N/A");

                    // ==========================================================
                    // LÓGICA FINAL PARA CARGAR LA FOTO (Maneja URI y RUTAS DE ARCHIVO)
                    // ==========================================================
                    if (fotoPath != null && !fotoPath.isEmpty()) {

                        boolean imageLoaded = false;

                        // 1. Intentar cargar como RUTA DE ARCHIVO (Para fotos de la cámara)
                        File imgFile = new File(fotoPath);
                        if (imgFile.exists() && imgFile.canRead()) {
                            try {
                                // Cargamos el archivo directamente como Bitmap
                                ivProfilePicture.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
                                imageLoaded = true;
                            } catch (Exception e) {
                                // Falla de archivo
                            }
                        }

                        // 2. Si no cargó como archivo, intentar cargar como URI (Galería o FileProvider)
                        if (!imageLoaded) {
                            Uri imageUri = Uri.parse(fotoPath);
                            try {
                                // Intento 2A: Cargar la URI directamente.
                                ivProfilePicture.setImageURI(imageUri);
                                imageLoaded = true;
                            } catch (SecurityException e) {
                                // Error de Seguridad (pérdida de permiso), reintentamos obtenerlo.
                                try {
                                    // 2B: Reintentar obtener el acceso persistente (Clave para Galería)
                                    int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                    getContentResolver().takePersistableUriPermission(imageUri, takeFlags);

                                    // Reintentar cargar la URI
                                    ivProfilePicture.setImageURI(imageUri);
                                    imageLoaded = true;
                                } catch (Exception ex) {
                                    // Falla total al cargar la URI. imageLoaded sigue siendo false.
                                }
                            } catch (Exception e) {
                                // URI mal formada o error genérico. imageLoaded sigue siendo false.
                            }
                        }

                        // 3. Manejo de error final y visualización por defecto
                        if (!imageLoaded) {
                            ivProfilePicture.setImageResource(android.R.drawable.ic_menu_camera);
                            // Este Toast indica que falló la carga por las razones anteriores (archivo movido, URI caducada, etc.)
                            Toast.makeText(this, "Advertencia: La foto de perfil anterior no se pudo cargar.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        // Si no hay foto, usar la imagen por defecto
                        ivProfilePicture.setImageResource(android.R.drawable.ic_menu_camera);
                    }
                    // ==========================================================


                } catch (IllegalArgumentException e) {
                    Toast.makeText(this, "Error al leer columnas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else {
                Toast.makeText(this, "No se encontraron datos para el usuario.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Error: No se encontró el ID de sesión.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Configura los listeners de clic para las opciones del menú de perfil.
     */
    private void setupMenuListeners() {
        llAjustes.setOnClickListener(v ->
                Toast.makeText(this, "Navegando a Ajustes", Toast.LENGTH_SHORT).show()
        );
    }
}
