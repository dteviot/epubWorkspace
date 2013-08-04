package com.dteviot.epubviewer.epub;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/*
 * Navpoint entry in a Table of Contents.
 */
public class NavPoint implements Parcelable {
    private int mPlayOrder;
    private String mNavLabel;
    private String mContent;
    
    public int getPlayOrder() { return mPlayOrder; }
    public String getNavLabel() { return mNavLabel; }
    public String getContent() { return mContent; }

    /*
     * Sometimes the content (resourceName) contains a tag 
     * into the HTML. 
     */
    public Uri getContentWithoutTag() {
        int indexOf = mContent.indexOf('#');
        String temp = mContent; 
        if (0 < indexOf) {
            temp = mContent.substring(0, indexOf);   
        }
        return Book.resourceName2Url(temp);
    }
    
    
    public void setPlayOrder(int playOrder) { mPlayOrder = playOrder; }
    public void setNavLabel(String navLabel) { mNavLabel = navLabel; }
    public void setContent(String content) { mContent = content; }
    
    /*
     * Construct as part of reading from XML
     */
    public NavPoint(String playOrder) {
       mPlayOrder = Integer.parseInt(playOrder); 
    }
    
    public NavPoint(Parcel in) {
        mPlayOrder = in.readInt();
        mNavLabel = in.readString();
        mContent = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mPlayOrder);
        dest.writeString(mNavLabel);
        dest.writeString(mContent);
    }

    public static final Parcelable.Creator<NavPoint> CREATOR
        = new Parcelable.Creator<NavPoint>() {
        public NavPoint createFromParcel(Parcel in) {
            return new NavPoint(in);
        }
        
        public NavPoint[] newArray(int size) {
            return new NavPoint[size];
        }
    };

}
