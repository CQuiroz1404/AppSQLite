package com.example.appconsqlite.ui.auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import de.hdodenhof.circleimageview.CircleImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;

import com.example.appconsqlite.R;
import com.example.appconsqlite.data.repository.UserRepository;

public class Registro extends AppCompatActivity {

    private EditText editNombre, editApellido, editDireccion;
    private TextInputEditText editTelefono;
    private EditText editEmail, editPassword, editConfirmPassword;
    private Button btnRegister;
    private ImageButton btnSelectProfileImage;
    private CircleImageView ivRegistroProfilePicture;

    private UserRepository userRepo;
    private Uri currentPhotoUri;
    private String currentPhotoPath; // Ruta absoluta del archivo de cámara.

    // Variable para almacenar la ruta final de la imagen a guardar en la DB (Path)
    private String profileImagePath = "";

    // ActivityResultLaunchers para manejar los resultados de Intents
    private ActivityResultLauncher<String[]> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> requestGalleryPermissionLauncher; // Launcher para permiso de galería
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        // Configurar la barra de estado con color rojo
        getWindow().setStatusBarColor(getResources().getColor(R.color.red_primary, null));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background_light_gray, null));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas: CAMPOS DE PERFIL
        ivRegistroProfilePicture = findViewById(R.id.ivRegistroProfilePicture);
        btnSelectProfileImage = findViewById(R.id.btnSelectProfileImage);
        editNombre = findViewById(R.id.editNombre);
        editApellido = findViewById(R.id.editApellido);
        editTelefono = findViewById(R.id.editTelefono);
        editDireccion = findViewById(R.id.editDireccion);

        // Inicializar vistas: LOGIN
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // Inicializar repositorio
        userRepo = new UserRepository(this);

        // Configuración de los launchers
        setupActivityResultLaunchers();

        // Listener para seleccionar la foto
        btnSelectProfileImage.setOnClickListener(v -> showImageSourceDialog());

        // Lógica de registro
        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    // Métodos de cámara/galería
    private void setupActivityResultLaunchers() {
        // Lanzador para permisos de cámara
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                    boolean cameraGranted = permissions.getOrDefault(Manifest.permission.CAMERA, false);
                    if (cameraGranted) {
                        startCameraIntent();
                    } else {
                        Toast.makeText(this, "Permiso de cámara denegado.", Toast.LENGTH_LONG).show();
                    }
                });

        // Lanzador para permiso de galería
        requestGalleryPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        startGalleryIntent();
                    } else {
                        Toast.makeText(this, "Permiso para acceder a la galería denegado.", Toast.LENGTH_LONG).show();
                    }
                });


        // Lanzador para el resultado de la cámara
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (currentPhotoUri != null) {
                            ivRegistroProfilePicture.setImageURI(currentPhotoUri);
                            // Usamos la ruta de archivo persistente (Path)
                            profileImagePath = currentPhotoPath;
                        }
                    }
                });

        // Lanzador para el resultado de la galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                // Copiamos la URI a un archivo privado para tener un path persistente
                                String internalPath = copyUriToInternalStorage(selectedImageUri);
                                ivRegistroProfilePicture.setImageURI(selectedImageUri); // Previsualización
                                // Guardamos la RUTA DE ARCHIVO INTERNA (Path)
                                profileImagePath = internalPath;

                            } catch (IOException e) {
                                Toast.makeText(this, "Error al procesar la imagen de la Galería.", Toast.LENGTH_LONG).show();
                                profileImagePath = "";
                            }
                        }
                    }
                });
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Imagen");
        builder.setItems(new CharSequence[]{"Tomar Foto", "Seleccionar de Galería"}, (dialog, which) -> {
            switch (which) {
                case 0: // Tomar Foto
                    checkCameraPermissionAndStartCamera();
                    break;
                case 1: // Seleccionar de Galería
                    checkGalleryPermissionAndStart();
                    break;
            }
        });
        builder.show();
    }

    // Métodos de verificación de permisos
    private void checkCameraPermissionAndStartCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Solicita permiso de cámara
            requestCameraPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
        } else {
            // Si ya tiene permiso, inicia la cámara
            startCameraIntent();
        }
    }

    // Método para verificar y pedir permiso de galería
    private void checkGalleryPermissionAndStart() {
        String permission;
        // En Android 13+ se usa READ_MEDIA_IMAGES. En versiones anteriores, READ_EXTERNAL_STORAGE.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            // Si el permiso ya está concedido, abre la galería
            startGalleryIntent();
        } else {
            // Si no, solicita el permiso
            requestGalleryPermissionLauncher.launch(permission);
        }
    }

    // Métodos para iniciar intents
    private void startCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            // Crear el archivo donde debe ir la foto
            File photoFile = createImageFile();
            currentPhotoPath = photoFile.getAbsolutePath(); // Guardar la ruta absoluta del archivo
            currentPhotoUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
            cameraLauncher.launch(takePictureIntent);
        } catch (IOException ex) {
            Toast.makeText(this, "Error al crear archivo de imagen.", Toast.LENGTH_LONG).show();
        }
    }

    private void startGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    // Métodos de manejo de archivos
    private String copyUriToInternalStorage(Uri uri) throws IOException {
        String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
        File targetFile = new File(getFilesDir(), fileName);

        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(targetFile)) {

            if (inputStream == null) throw new IOException("No se pudo abrir el InputStream desde la URI.");

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return targetFile.getAbsolutePath();
    }

    private File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefijo */
                ".jpg",         /* sufijo */
                storageDir      /* directorio */
        );
    }


    // Lógica de registro
    private void handleRegistration() {
        String nombre = editNombre.getText().toString().trim();
        String apellido = editApellido.getText().toString().trim();
        String telefono = editTelefono.getText().toString().trim();
        String direccion = editDireccion.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios (*)", Toast.LENGTH_LONG).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Formato de email inválido", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show();
            return;
        }

        boolean creado = userRepo.registrarUsuario(
                nombre,
                apellido,
                email,
                password,
                telefono,
                direccion,
                profileImagePath
        );

        if (creado) {
            Toast.makeText(this, "¡Bienvenido al Marketplace, " + nombre + "!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Error: El email ya está registrado", Toast.LENGTH_LONG).show();
        }
    }
}
