package com.dteviot.epubviewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

public class Bookmark{
    public static final String PREFS_NAME = "EpubViewerPrefsFile";
    public static final String PREFS_EPUB_NAME = "FileName";
    public static final String PREFS_RESOURCE_URI = "ResourceUri";
    public static final String PREFS_SCROLLY = "ScrollY";

    public String mFileName;
    public Uri mResourceUri;
    public float mScrollY;

    /*
     * ctor
     */
    public Bookmark(String fileName, Uri resourceUri, float scrollY) {
        mFileName = fileName;
        mResourceUri = resourceUri;
        mScrollY = scrollY;
    }
    
    /*
     * Called when user changed screen orientation
     */
    public Bookmark(Bundle savedInstanceState) {
        mFileName = savedInstanceState.getString(PREFS_EPUB_NAME);
        deserializeUri(savedInstanceState.getString(PREFS_RESOURCE_URI));
        mScrollY = savedInstanceState.getFloat(PREFS_SCROLLY);
    }

    /*
     * Retrieve from the Shared Preferences
     */
    public Bookmark(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mFileName = settings.getString(PREFS_EPUB_NAME, "");
        deserializeUri(settings.getString(PREFS_RESOURCE_URI, ""));
        mScrollY = settings.getFloat(PREFS_SCROLLY, 0.0f);
    }

    private void deserializeUri(String uri) {
        if ((uri != null) && !uri.isEmpty()) {
            mResourceUri = Uri.parse(uri);
        }
    }
    
    /*
     * return true if bookmark is "empty", i.e. doesn't hold a useful value
     */
    public boolean isEmpty() {
        return ((mFileName == null) || (mFileName.length() <= 0) 
                || (mResourceUri == null));
    }

    /*
     * Write the bookmark into a bundle (normally used when screen orientation
     * changing)
     */
    public void save(Bundle outState) {
        if (!isEmpty()) {
            outState.putString(PREFS_EPUB_NAME, mFileName);
            outState.putString(PREFS_RESOURCE_URI, mResourceUri.toString());
            outState.putFloat(PREFS_SCROLLY, mScrollY);
        }
    }

    /*
     * Write to persistent storage
     */
    public void saveToSharedPreferences(Context context) {
        if (!isEmpty()) {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(PREFS_EPUB_NAME, mFileName);
            editor.putString(PREFS_RESOURCE_URI, mResourceUri.toString());
            editor.putFloat(PREFS_SCROLLY, mScrollY);
            editor.commit();
        }
    }

    /*
     * The epub that has been bookmarked
     */
    public String getFileName() {
        return mFileName;
    }

    /*
     * Chapter of book
     */
    public Uri getResourceUri() {
        return mResourceUri;
    }

    /*
     * Position of chapter
     */
    public float getScrollY() {
        return mScrollY;
    }
}
