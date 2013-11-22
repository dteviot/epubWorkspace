package com.dteviot.epubviewer.epub;

import org.xml.sax.Attributes;

import com.dteviot.epubviewer.HrefResolver;
import com.dteviot.epubviewer.Utility;

/*
 * A row in the epub Manifest
 */
public class ManifestItem {
    private static final String XML_ATTRIBUTE_ID = "id";
    private static final String XML_ATTRIBUTE_HREF = "href";
    private static final String XML_ATTRIBUTE_MEDIA_TYPE = "media-type";

    private String mHref;
    private String mID;
    private String mMediaType;
    
    public String getHref() { return mHref; }
    public String getID() { return mID; }
    public String getMediaType() { return mMediaType; }

    /*
     * Construct from XML
     */
    public ManifestItem(Attributes attributes, HrefResolver resolver) {
        mHref = resolver.ToAbsolute(attributes.getValue(XML_ATTRIBUTE_HREF));       
        mID = attributes.getValue(XML_ATTRIBUTE_ID);       
        mMediaType = attributes.getValue(XML_ATTRIBUTE_MEDIA_TYPE);       
    }
}
