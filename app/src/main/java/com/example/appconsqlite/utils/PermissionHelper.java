package com.example.appconsqlite.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;

// Maneja permisos de cámara y galería según versión de Android
public class PermissionHelper {

    // Verifica si el permiso de cámara está otorgado
    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Verifica permiso de galería (READ_MEDIA_IMAGES en Android 13+, READ_EXTERNAL_STORAGE en versiones anteriores)
    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Retorna el permiso de almacenamiento correcto según API level
    public static String getStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            return Manifest.permission.READ_EXTERNAL_STORAGE;
        }
    }

    // Retorna array con permiso de cámara (usamos FileProvider, no necesitamos WRITE_EXTERNAL_STORAGE)
    public static String[] getCameraPermissions() {
        return new String[]{Manifest.permission.CAMERA};
    }

    // Retorna array con permiso de galería según versión de Android
    public static String[] getGalleryPermissions() {
        return new String[]{getStoragePermission()};
    }
}

