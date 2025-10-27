package com.example.appconsqlite.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

// Gestiona almacenamiento, redimensionamiento y eliminación de imágenes de productos
public class ImageHelper {
    private static final String TAG = "ImageHelper";
    private static final String IMAGE_FOLDER = "product_images";
    private static final int MAX_IMAGE_SIZE = 1024;

    // Copia imagen desde URI a almacenamiento interno y la redimensiona si es necesario
    public static String saveImageToInternalStorage(Context context, Uri imageUri, String productName) {
        if (imageUri == null) {
            return null;
        }

        try {
            File imageFolder = new File(context.getFilesDir(), IMAGE_FOLDER);
            if (!imageFolder.exists()) {
                imageFolder.mkdirs();
            }

            String fileName = "product_" + System.currentTimeMillis() + "_" +
                             sanitizeFileName(productName) + ".jpg";
            File imageFile = new File(imageFolder, fileName);

            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "No se pudo abrir el InputStream de la URI");
                return null;
            }

            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) {
                Log.e(TAG, "No se pudo decodificar la imagen");
                return null;
            }

            Bitmap resizedBitmap = resizeImage(originalBitmap, MAX_IMAGE_SIZE);

            OutputStream outputStream = new FileOutputStream(imageFile);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            outputStream.flush();
            outputStream.close();

            if (resizedBitmap != originalBitmap) {
                originalBitmap.recycle();
            }
            resizedBitmap.recycle();

            Log.d(TAG, "Imagen guardada exitosamente en: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "Error al guardar la imagen: " + e.getMessage(), e);
            return null;
        }
    }

    // Redimensiona imagen si excede el tamaño máximo manteniendo proporción
    private static Bitmap resizeImage(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return image;
        }

        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
    }

    // Elimina caracteres no válidos del nombre de archivo
    private static String sanitizeFileName(String name) {
        if (name == null || name.isEmpty()) {
            return "image";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_").substring(0, Math.min(name.length(), 30));
    }

    // Elimina imagen del almacenamiento interno
    public static boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }

        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            boolean deleted = imageFile.delete();
            Log.d(TAG, "Imagen eliminada: " + deleted);
            return deleted;
        }
        return false;
    }
}

