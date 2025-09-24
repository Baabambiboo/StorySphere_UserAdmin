package com.example.storysphere_appbar;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageStorageHelper {

    /**
     * Saves a Bitmap image to a specified directory within the app's internal storage.
     * @param context The application context.
     * @param bitmap The Bitmap image to save.
     * @param directoryName The name of the internal directory (e.g., "profile_images", "writing_images").
     * @param fileName The desired file name (without extension). A ".jpg" extension will be added.
     * @return The String representation of the Uri of the saved image file, or null if saving failed.
     */
    public static String saveImageToInternalStorage(Context context, Bitmap bitmap, String directoryName, String fileName) {
        // Create a directory for images within the app's internal storage
        // This directory is private to your app.
        File directory = context.getDir(directoryName, Context.MODE_PRIVATE); // Now accepts directoryName
        // Create the image file
        File mypath = new File(directory, fileName + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Compress the Bitmap to the file (JPEG format, 100% quality)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush(); // Ensure all data is written
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null if an error occurred during saving
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Return the String representation of the Uri of the saved file
        // This URI can be stored in the database.
        return Uri.fromFile(mypath).toString();
    }

    /**
     * Deletes an image file from internal storage given its URI string.
     * @param context The application context.
     * @param imageUriString The String URI of the image to delete.
     * @return true if the file was successfully deleted, false otherwise.
     */
    public static boolean deleteImageFromInternalStorage(Context context, String imageUriString) {
        if (imageUriString == null || imageUriString.isEmpty()) {
            return false;
        }
        try {
            Uri imageUri = Uri.parse(imageUriString);
            File fileToDelete = new File(imageUri.getPath());
            if (fileToDelete.exists()) {
                return fileToDelete.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
