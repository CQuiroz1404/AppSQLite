package com.example.appconsqlite.ui.profile;

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

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appconsqlite.R;
import com.example.appconsqlite.data.repository.UserRepository;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditarPerfilActivity extends AppCompatActivity {

    private EditText editNombre, editApellido, editDireccion;
    private TextInputEditText editTelefono;
    private Button btnSaveChanges;
    private ImageButton btnSelectProfileImage;
    private CircleImageView ivProfilePicture;

    private UserRepository userRepo;
    private String userEmail;
    private long userId;
    private Uri currentPhotoUri;
    private String currentPhotoPath;
    private String profileImagePath = "";

    private ActivityResultLauncher<String[]> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> requestGalleryPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_perfil);

        getWindow().setStatusBarColor(getResources().getColor(R.color.red_primary, null));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background_light_gray, null));

        // Asocia las vistas con sus IDs correctos del XML
        ivProfilePicture = findViewById(R.id.ivPerfilEditar);
        btnSelectProfileImage = findViewById(R.id.btnSeleccionarFoto);
        editNombre = findViewById(R.id.etNombre);
        editApellido = findViewById(R.id.etApellido);
        editTelefono = findViewById(R.id.etTelefono);
        editDireccion = findViewById(R.id.etDireccion);
        btnSaveChanges = findViewById(R.id.btnGuardarCambios);

        userRepo = new UserRepository(this);

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Error: No se pudo obtener el email del usuario.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Obtenemos el ID del usuario a partir del email
        userId = userRepo.getUserIdByEmail(userEmail);
        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no encontrado.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupActivityResultLaunchers();
        loadUserProfile();

        btnSelectProfileImage.setOnClickListener(v -> showImageSourceDialog());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void setupActivityResultLaunchers() {
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                    boolean cameraGranted = permissions.getOrDefault(Manifest.permission.CAMERA, false);
                    if (cameraGranted) {
                        startCameraIntent();
                    } else {
                        Toast.makeText(this, "Permiso de cámara denegado.", Toast.LENGTH_LONG).show();
                    }
                });

        requestGalleryPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        startGalleryIntent();
                    } else {
                        Toast.makeText(this, "Permiso para acceder a la galería denegado.", Toast.LENGTH_LONG).show();
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (currentPhotoUri != null) {
                            ivProfilePicture.setImageURI(currentPhotoUri);
                            profileImagePath = currentPhotoPath;
                        }
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                String internalPath = copyUriToInternalStorage(selectedImageUri);
                                ivProfilePicture.setImageURI(selectedImageUri);
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

    private void checkCameraPermissionAndStartCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
        } else {
            startCameraIntent();
        }
    }

    private void checkGalleryPermissionAndStart() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            startGalleryIntent();
        } else {
            requestGalleryPermissionLauncher.launch(permission);
        }
    }

    private void startCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = createImageFile();
            currentPhotoPath = photoFile.getAbsolutePath();
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
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void loadUserProfile() {
        // Aquí se cargarían los datos del usuario.
    }

    private void saveChanges() {
        String nombre = editNombre.getText().toString().trim();
        String apellido = editApellido.getText().toString().trim();
        String telefono = editTelefono.getText().toString().trim();
        String direccion = editDireccion.getText().toString().trim();

        if (nombre.isEmpty() || apellido.isEmpty()) {
            Toast.makeText(this, "Nombre y Apellido son obligatorios.", Toast.LENGTH_LONG).show();
            return;
        }

        boolean actualizado = userRepo.actualizarPerfil(
                userId,
                nombre,
                apellido,
                telefono,
                direccion,
                profileImagePath
        );

        if (actualizado) {
            Toast.makeText(this, "Perfil actualizado correctamente.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Error al actualizar el perfil.", Toast.LENGTH_LONG).show();
        }
    }
}
