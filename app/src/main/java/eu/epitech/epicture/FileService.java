package eu.epitech.epicture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by lucien on 08/02/18.
 */

public class FileService {

    public static void showFileChooser(Context context) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        try {
            ((Activity)context).startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    //intent,
                    0
            );
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(context, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

}
