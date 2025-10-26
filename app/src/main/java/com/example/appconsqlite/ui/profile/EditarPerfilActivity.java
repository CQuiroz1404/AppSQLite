package com.example.appconsqlite.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.appconsqlite.R;
import com.example.appconsqlite.ui.auth.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

import com.example.appconsqlite.data.repository.UserRepository;
import com.example.appconsqlite.data.database.UserContract;
import com.example.appconsqlite.utils.SessionManager;

public class EditarPerfilActivity extends AppCompatActivity {

    private CircleImageView ivPerfilEditar;
    private ImageButton btnSeleccionarFoto;
    private TextInputEditText etNombre, etApellido, etTelefono, etDireccion, etEmail;
    private TextInputLayout tilNombre, tilApellido, tilTelefono, tilDireccion, tilEmail;
    private Button btnGuardarCambios, btnCambiarPassword, btnEliminarCuenta;

    private UserRepository userRepo;
    private SessionManager sessionManager;
    private long userId;
    private String fotoPerfilPath = "";

    // Launcher para seleccionar imagen
    private ActivityResultLauncher<Intent> galeriaLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        // Configurar la barra de estado con color rojo
        getWindow().setStatusBarColor(getResources().getColor(R.color.red_primary, null));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background_light_gray, null));

        // Inicializar vistas
        ivPerfilEditar = findViewById(R.id.ivPerfilEditar);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etTelefono = findViewById(R.id.etTelefono);
        etDireccion = findViewById(R.id.etDireccion);
        etEmail = findViewById(R.id.etEmail);

        tilNombre = findViewById(R.id.tilNombre);
        tilApellido = findViewById(R.id.tilApellido);
        tilTelefono = findViewById(R.id.tilTelefono);
        tilDireccion = findViewById(R.id.tilDireccion);
        tilEmail = findViewById(R.id.tilEmail);

        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        btnCambiarPassword = findViewById(R.id.btnCambiarPassword);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);

        userRepo = new UserRepository(this);

        // Obtener userId desde SessionManager
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar launcher para galería
        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            ivPerfilEditar.setImageURI(imageUri);
                            fotoPerfilPath = imageUri.toString();

                            // Obtener permiso persistente
                            try {
                                int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                            } catch (SecurityException e) {
                                Toast.makeText(this, "No se pudo obtener permiso persistente para la imagen", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        // Cargar datos actuales
        cargarDatosUsuario();

        // Configurar listeners
        btnSeleccionarFoto.setOnClickListener(v -> abrirGaleria());
        btnGuardarCambios.setOnClickListener(v -> guardarCambios());
        btnCambiarPassword.setOnClickListener(v -> mostrarDialogCambiarPassword());
        btnEliminarCuenta.setOnClickListener(v -> mostrarDialogEliminarCuenta());
    }

    private void cargarDatosUsuario() {
        Cursor cursor = userRepo.obtenerDatosPerfil(userId);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_NOMBRE));
                String apellido = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_APELLIDO));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_EMAIL));
                String telefono = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_TELEFONO));
                String direccion = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_DIRECCION));
                String fotoPath = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_FOTO_PERFIL_PATH));

                etNombre.setText(nombre);
                etApellido.setText(apellido);
                etEmail.setText(email);
                etTelefono.setText(telefono);
                etDireccion.setText(direccion);
                fotoPerfilPath = fotoPath != null ? fotoPath : "";

                // Cargar foto
                if (fotoPath != null && !fotoPath.isEmpty()) {
                    File imgFile = new File(fotoPath);
                    if (imgFile.exists()) {
                        ivPerfilEditar.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
                    } else {
                        try {
                            ivPerfilEditar.setImageURI(Uri.parse(fotoPath));
                        } catch (Exception e) {
                            ivPerfilEditar.setImageResource(android.R.drawable.ic_menu_camera);
                        }
                    }
                } else {
                    ivPerfilEditar.setImageResource(android.R.drawable.ic_menu_camera);
                }
            } finally {
                cursor.close();
            }
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galeriaLauncher.launch(intent);
    }

    private void guardarCambios() {
        // Limpiar errores
        tilNombre.setError(null);
        tilApellido.setError(null);
        tilEmail.setError(null);
        tilTelefono.setError(null);
        tilDireccion.setError(null);

        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(nombre)) {
            tilNombre.setError("El nombre no puede estar vacío");
            isValid = false;
        }

        if (TextUtils.isEmpty(apellido)) {
            tilApellido.setError("El apellido no puede estar vacío");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("El email no puede estar vacío");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Formato de email inválido");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Actualizar perfil
        boolean exito = userRepo.actualizarPerfil(userId, nombre, apellido, telefono, direccion, fotoPerfilPath);

        if (exito) {
            // Actualizar email en SessionManager si cambió
            String emailActual = sessionManager.getUserEmail();
            if (!email.equals(emailActual)) {
                // Actualizar email en la base de datos y en SessionManager
                sessionManager.createSession(userId, email);
            }

            Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogCambiarPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambiar Contraseña");

        // Crear layout personalizado
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etPasswordAntigua = new EditText(this);
        etPasswordAntigua.setHint("Contraseña actual");
        etPasswordAntigua.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPasswordAntigua);

        final EditText etPasswordNueva = new EditText(this);
        etPasswordNueva.setHint("Contraseña nueva");
        etPasswordNueva.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPasswordNueva);

        final EditText etPasswordConfirmar = new EditText(this);
        etPasswordConfirmar.setHint("Confirmar contraseña nueva");
        etPasswordConfirmar.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPasswordConfirmar);

        builder.setView(layout);

        builder.setPositiveButton("Cambiar", (dialog, which) -> {
            String passwordAntigua = etPasswordAntigua.getText().toString();
            String passwordNueva = etPasswordNueva.getText().toString();
            String passwordConfirmar = etPasswordConfirmar.getText().toString();

            if (TextUtils.isEmpty(passwordAntigua) || TextUtils.isEmpty(passwordNueva)) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!passwordNueva.equals(passwordConfirmar)) {
                Toast.makeText(this, "Las contraseñas nuevas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            if (passwordNueva.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean cambiada = userRepo.cambiarPassword(userId, passwordAntigua, passwordNueva);

            if (cambiada) {
                Toast.makeText(this, "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDialogEliminarCuenta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚠️ Eliminar Cuenta");
        builder.setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer y se eliminarán todos tus productos.");

        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            boolean eliminada = userRepo.eliminarCuenta(userId);

            if (eliminada) {
                // Cerrar sesión usando SessionManager
                sessionManager.logout();

                Toast.makeText(this, "Cuenta eliminada", Toast.LENGTH_SHORT).show();

                // Volver al login
                Intent intent = new Intent(EditarPerfilActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}
