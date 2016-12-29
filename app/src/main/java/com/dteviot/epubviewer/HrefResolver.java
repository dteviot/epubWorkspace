package com.dteviot.epubviewer;

import android.net.Uri;

/*
 * Converts relative href to absolute
 */
public class HrefResolver {
    /*
     * path to file holding the href
     */
    private String mParentPath;
    
    public HrefResolver(String parentFileName) {
    	mParentPath = Utility.extractPath(parentFileName); 
    }
    
    public String ToAbsolute(String relativeHref) {
        String decoded = Uri.decode(relativeHref);
        return Utility.concatPath(mParentPath, decoded);
    }
}
