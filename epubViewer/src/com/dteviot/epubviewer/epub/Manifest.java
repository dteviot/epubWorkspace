package com.dteviot.epubviewer.epub;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * The manifest section of the epub's metadata
 */
public class Manifest {
    private ArrayList<ManifestItem> mItems;
    private HashMap<String, ManifestItem> idIndex; 
    
    
    public Manifest() {
        mItems = new ArrayList<ManifestItem>();
        idIndex = new HashMap<String, ManifestItem>();
    }
    
    public void add(ManifestItem item) {
        mItems.add(item);
        idIndex.put(item.getID(), item);
    }
    
    public void clear() {
        mItems.clear();
    }
    
    public ManifestItem findById(String id) {
        return idIndex.get(id);
    }
    
    public ManifestItem findByResourceName(String resourceName) {
        for(int i = 0; i < mItems.size(); ++i) {
            ManifestItem item = mItems.get(i);
            if (resourceName.equals(item.getHref())) {
                return item;
            }
        }
        return null;
    }
    
    /*
     * For Unit Testing
     */
    public ArrayList<ManifestItem> getItems() { return mItems; }
}
