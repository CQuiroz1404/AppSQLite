package com.example.appconsqlite;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import de.hdodenhof.circleimageview.CircleImageView;

import android.graphics.BitmapFactory;
import java.io.File;

import com.example.appconsqlite.utils.SessionManager;
import com.example.appconsqlite.data.repository.UserRepository;
import com.example.appconsqlite.data.database.UserContract;

public class Perfil extends AppCompatActivity {

    // Vistas principales para mostrar datos
    private CircleImageView ivProfilePicture;
    private TextView tvUserName;

    // Vistas para la información de contacto
    private TextView tvUserEmail;
    private TextView tvUserPhone;
    private TextView tvUserAddress;

    private SessionManager sessionManager;
    private UserRepository userRepo;

    // Vistas del menú de opciones
    private LinearLayout llAjustes;
    private LinearLayout llEditarPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // FORZAR MODO CLARO
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        // Configurar la barra de estado con color rojo
        getWindow().setStatusBarColor(getResources().getColor(R.color.red_primary, null));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background_light_gray, null));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar SessionManager y repositorio
        sessionManager = new SessionManager(this);
        userRepo = new UserRepository(this);

        // 1. Inicializar las vistas de datos
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvUserAddress = findViewById(R.id.tvUserAddress);

        // 2. Inicializar los contenedores de las opciones de menú
        llAjustes = findViewById(R.id.llAjustes);
        llEditarPerfil = findViewById(R.id.llEditarPerfil);

        setupMenuListeners();

        // 3. Cargar datos del usuario
        loadUserData();
    }

    /**
     * Carga los datos del usuario logeado desde la base de datos.
     */
    private void loadUserData() {
        // Obtener el ID del usuario desde SessionManager
        long userId = sessionManager.getUserId();

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
                            }
                        }

                        // 3. Si nada funcionó, usar imagen por defecto
                        if (!imageLoaded) {
                            ivProfilePicture.setImageResource(R.mipmap.ic_launcher);
                        }
                    } else {
                        // Sin foto de perfil guardada
                        ivProfilePicture.setImageResource(R.mipmap.ic_launcher);
                    }

                } finally {
                    cursor.close();
                }
            } else {
                Toast.makeText(this, "Error al cargar datos del perfil", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Configura los listeners de clic para las opciones del menú de perfil.
     */
    private void setupMenuListeners() {
        llAjustes.setOnClickListener(v ->
                Toast.makeText(this, "Navegando a Ajustes", Toast.LENGTH_SHORT).show()
        );

        llEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(Perfil.this, EditarPerfilActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos cuando volvemos de editar
        loadUserData();
    }
}
