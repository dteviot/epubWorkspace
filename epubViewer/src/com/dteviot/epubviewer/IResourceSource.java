package com.dteviot.epubviewer;

import android.net.Uri;

public interface IResourceSource {
    /*
     * Fetch the requested resource
     */
    public ResourceResponse fetch(Uri resourceUri);
}
