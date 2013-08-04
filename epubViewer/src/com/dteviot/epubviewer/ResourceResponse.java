package com.dteviot.epubviewer;

import java.io.InputStream;

/*
 * A POJO, basically a cut down WebResourceResponse
 * for use on Android devices below 3.0 
 */
public class ResourceResponse {
    private String mMimeType;
    private InputStream mData;
    private long mSize;
    
    public ResourceResponse(String mimeType, InputStream data) {
        mMimeType = mimeType;
        mData = data;
    }
    
    public String getMimeType() { return mMimeType; }
    public InputStream getData() { return mData; }
    public long getSize() { return mSize; }
    public void setMimeType(String mimeType) { mMimeType = mimeType; }
    public void setData(InputStream data) { mData = data; }
    public void setSize(long size) { mSize = size; }
}
