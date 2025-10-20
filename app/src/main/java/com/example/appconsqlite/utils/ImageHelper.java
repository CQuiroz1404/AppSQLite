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

public class ImageHelper {
    private static final String TAG = "ImageHelper";
    private static final String IMAGE_FOLDER = "product_images";
    private static final int MAX_IMAGE_SIZE = 1024; // Tamaño máximo en píxeles

    /**
     * Copia una imagen desde una URI a la carpeta interna de la app
     * @param context Contexto de la aplicación
     * @param imageUri URI de la imagen seleccionada
     * @param productName Nombre del producto (para nombrar el archivo)
     * @return Ruta absoluta del archivo guardado, o null si falla
     */
    public static String saveImageToInternalStorage(Context context, Uri imageUri, String productName) {
        if (imageUri == null) {
            return null;
        }

        try {
            // Crear carpeta para imágenes si no existe
            File imageFolder = new File(context.getFilesDir(), IMAGE_FOLDER);
            if (!imageFolder.exists()) {
                imageFolder.mkdirs();
            }

            // Crear nombre único para el archivo
            String fileName = "product_" + System.currentTimeMillis() + "_" +
                             sanitizeFileName(productName) + ".jpg";
            File imageFile = new File(imageFolder, fileName);

            // Leer la imagen desde la URI
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "No se pudo abrir el InputStream de la URI");
                return null;
            }

            // Decodificar la imagen
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) {
                Log.e(TAG, "No se pudo decodificar la imagen");
                return null;
            }

            // Redimensionar si es necesario
            Bitmap resizedBitmap = resizeImage(originalBitmap, MAX_IMAGE_SIZE);

            // Guardar la imagen comprimida
            OutputStream outputStream = new FileOutputStream(imageFile);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            outputStream.flush();
            outputStream.close();

            // Liberar memoria
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

    /**
     * Redimensiona una imagen si excede el tamaño máximo
     */
    private static Bitmap resizeImage(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return image;
        }

        float ratio = Math.min(
            (float) maxSize / width,
            (float) maxSize / height
        );

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
    }

    /**
     * Sanitiza el nombre del archivo eliminando caracteres no válidos
     */
    private static String sanitizeFileName(String name) {
        if (name == null || name.isEmpty()) {
            return "image";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_").substring(0, Math.min(name.length(), 30));
    }

    /**
     * Elimina una imagen del almacenamiento interno
     * @param imagePath Ruta absoluta de la imagen
     * @return true si se eliminó correctamente
     */
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

