package com.example.appconsqlite;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import de.hdodenhof.circleimageview.CircleImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream; // IMPORTACIÓN AÑADIDA
import java.io.FileOutputStream; // IMPORTACIÓN AÑADIDA

public class Registro extends AppCompatActivity {

    private EditText editNombre, editApellido, editTelefono, editDireccion;
    private EditText editEmail, editPassword, editConfirmPassword;
    private Button btnRegister;
    private ImageButton btnSelectProfileImage;
    private CircleImageView ivRegistroProfilePicture;

    private UserRepository userRepo;
    private Uri currentPhotoUri;
    private String currentPhotoPath; // Ruta absoluta del archivo de cámara.

    // Variable para almacenar la ruta final de la imagen a guardar en la DB (Path)
    private String profileImagePath = "";

    // 1. ActivityResultLaunchers para manejar los resultados de Intents
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

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

        // 3. Lógica de registro
        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    // ==========================================================
    // MÉTODOS DE CÁMARA/GALERÍA
    // ==========================================================

    private void setupActivityResultLaunchers() {
        // Inicializa el lanzador para solicitar permisos
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                    boolean cameraGranted = permissions.getOrDefault(Manifest.permission.CAMERA, false);
                    boolean storageGranted = permissions.getOrDefault(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);

                    if (cameraGranted && storageGranted) {
                        startCameraIntent();
                    } else {
                        Toast.makeText(this, "Permisos denegados. No se puede acceder a la cámara o galería.", Toast.LENGTH_LONG).show();
                    }
                });

        // Inicializa el lanzador para la Cámara
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

        // Inicializa el lanzador para la Galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                // FIX GALERÍA: Copiamos la URI a un archivo privado
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
                case 0:
                    checkCameraPermissionAndStartCamera();
                    break;
                case 1:
                    startGalleryIntent();
                    break;
            }
        });
        builder.show();
    }

    private void checkCameraPermissionAndStartCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
        } else {
            startCameraIntent();
        }
    }

    private void startCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                // Crear el archivo donde debe ir la foto
                File photoFile = createImageFile();
                if (photoFile != null) {
                    // Guardar la ruta absoluta del archivo
                    currentPhotoPath = photoFile.getAbsolutePath();

                    // Obtener URI con FileProvider
                    currentPhotoUri = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                    cameraLauncher.launch(takePictureIntent);
                }
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear archivo de imagen.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // MÉTODO AÑADIDO: Copia la URI a un archivo interno
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

    private void startGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    // ==========================================================
    // LÓGICA DE REGISTRO
    // ==========================================================

    private void handleRegistration() {
        String nombre = editNombre.getText().toString().trim();
        String apellido = editApellido.getText().toString().trim();
        String telefono = editTelefono.getText().toString().trim();
        String direccion = editDireccion.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        // 1. Validación de campos vacíos
        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios (*)", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Validación de coincidencia de contraseñas
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. Ya no se necesita lógica de permisos persistentes aquí.

        // 4. Llamada al repositorio con TODOS los datos
        boolean creado = userRepo.registrarUsuario(
                nombre,
                apellido,
                email,
                password,
                telefono,
                direccion,
                profileImagePath // Siempre será un Path de archivo (interno o externo)
        );

        if (creado) {
            Toast.makeText(this, "Usuario " + nombre + " registrado correctamente", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Error: El email ya está registrado", Toast.LENGTH_LONG).show();
        }
    }
}