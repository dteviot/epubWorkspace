package com.dteviot.epubviewer;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.widget.Toast;

/*
 * Assorted utility functions
 */
public class Utility {
    public static final String ERROR_STRING_ID_EXTRA = "ERROR_STRING_ID_EXTRA";

    public static void showToast(Context context, int stringId) {
        Toast msg = Toast.makeText(context, stringId, Toast.LENGTH_SHORT);
        msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, msg.getXOffset() / 2);
        msg.show();
    }
    
    public static void finishWithError(Activity activity, int stringId) {
        Intent intent = new Intent();
        intent.putExtra(ERROR_STRING_ID_EXTRA, stringId);
        activity.setResult(Activity.RESULT_CANCELED, intent);
        activity.finish();
    }
    
    public static void showErrorToast(Context context, Intent intent) {
        if (intent != null) {
            int stringId = intent.getIntExtra(ERROR_STRING_ID_EXTRA, 0);
            if (stringId != 0) {
                showToast(context, stringId);
            }
        }
    }
    
    /*
     * Return path part of a filename 
     */
    public static String extractPath(String fileName) {
        try {
            String path = new File(fileName).getCanonicalFile().getParent(); 
            // remove leading '/'
            return path == null ? "" : path.substring(1); 
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String concatPath(String basePath, String pathToAdd) {
        String rawPath = basePath + '/' + pathToAdd;
        if ((basePath == null) || basePath.isEmpty() || pathToAdd.startsWith("/")) {
            rawPath = pathToAdd;
        }
        try {
            return new File(rawPath).getCanonicalPath().substring(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
