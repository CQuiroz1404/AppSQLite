package com.example.appconsqlite.ui.auth;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import com.example.appconsqlite.utils.PermissionHelper;
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


    // Variable para rastrear qué acción inició la solicitud de permisos
    private boolean requestingCameraPermission = false;
    // Variable para almacenar la ruta final de la imagen a guardar en la DB (Path)
    private String profileImagePath = "";

    // 1. ActivityResultLaunchers para manejar los resultados de Intents
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // FORZAR MODO CLARO
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

        // ==========================================================
        // CONFIGURACIÓN DE LOS LAUNCHERS
        // ==========================================================
        setupActivityResultLaunchers();

        // 2. Listener para seleccionar la foto
        btnSelectProfileImage.setOnClickListener(v -> showImageSourceDialog());

        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    private void setupActivityResultLaunchers() {
        // Maneja respuesta de solicitud de permisos (cámara o galería según flag)
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                    boolean cameraGranted = permissions.getOrDefault(Manifest.permission.CAMERA, false);
                    boolean readExternal = permissions.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false);
                    boolean readMediaImages = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        readMediaImages = permissions.getOrDefault(Manifest.permission.READ_MEDIA_IMAGES, false);
                    }
                    boolean storageGranted = readExternal || readMediaImages;

                    if (requestingCameraPermission) {
                        if (cameraGranted) {
                            startCameraIntent();
                        } else {
                            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_LONG).show();
                        }
                        requestingCameraPermission = false;
                    } else {
                        if (storageGranted) {
                            startGalleryIntent();
                        } else {
                            Toast.makeText(this, "Permiso de galería denegado", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        // Maneja resultado de la cámara
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (currentPhotoUri != null) {
                            ivRegistroProfilePicture.setImageURI(currentPhotoUri);
                            profileImagePath = currentPhotoPath;
                        }
                    }
                });

        // Maneja resultado de la galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                String internalPath = copyUriToInternalStorage(selectedImageUri);
                                ivRegistroProfilePicture.setImageURI(selectedImageUri);
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
                case 0:
                    checkCameraPermissionAndStartCamera();
                    break;
                case 1:
                    checkGalleryPermissionAndOpen();
                    break;
            }
        });
        builder.show();
    }

    private void checkCameraPermissionAndStartCamera() {
        if (!PermissionHelper.hasCameraPermission(this)) {
            requestingCameraPermission = true;
            requestPermissionLauncher.launch(PermissionHelper.getCameraPermissions());
        } else {
            startCameraIntent();
        }
    }

    private void checkGalleryPermissionAndOpen() {
        if (!PermissionHelper.hasStoragePermission(this)) {
            requestingCameraPermission = false;
            requestPermissionLauncher.launch(PermissionHelper.getGalleryPermissions());
        } else {
            startGalleryIntent();
        }
    }

    private void startCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear archivo de imagen: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            if (photoFile != null) {
                try {
                    currentPhotoPath = photoFile.getAbsolutePath();
                    currentPhotoUri = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".fileprovider",
                            photoFile);

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    cameraLauncher.launch(takePictureIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "Error al abrir la cámara: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(this, "No se encontró una aplicación de cámara", Toast.LENGTH_LONG).show();
        }
    }

    // Copia imagen de URI externa a almacenamiento interno de la app
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

    // Crea archivo temporal para la foto con timestamp único
    private File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);

        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        if (storageDir == null) {
            throw new IOException("No se pudo acceder al directorio de almacenamiento");
        }

        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void startGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void handleRegistration() {
        String nombre = editNombre.getText().toString().trim();
        String apellido = editApellido.getText().toString().trim();
        String telefono = editTelefono.getText().toString().trim();
        String direccion = editDireccion.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        // Validar que campos obligatorios no estén vacíos
        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios (*)", Toast.LENGTH_LONG).show();
            return;
        }

        // Validar formato correcto de email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Formato de email inválido", Toast.LENGTH_LONG).show();
            return;
        }

        // Validar que las contraseñas coincidan
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