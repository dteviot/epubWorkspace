package com.dteviot.epubviewer.epub;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import com.dteviot.epubviewer.HrefResolver;

import android.content.Intent;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;

/*
 * The TableOfContents part of the epub
 */
public class TableOfContents {
    private static final String XML_NAMESPACE_TABLE_OF_CONTENTS = "http://www.daisy.org/z3986/2005/ncx/"; 
    private static final String XML_ELEMENT_NCX = "ncx";
    private static final String XML_ELEMENT_NAVMAP = "navMap";
    private static final String XML_ELEMENT_NAVPOINT = "navPoint";
    private static final String XML_ELEMENT_NAVLABEL = "navLabel";
    private static final String XML_ELEMENT_TEXT = "text";
    private static final String XML_ELEMENT_CONTENT = "content";
    private static final String XML_ATTRIBUTE_PLAYORDER = "playOrder";
    private static final String XML_ATTRIBUTE_SCR = "src";
    
    private ArrayList<NavPoint> mNavPoints;
    
    private int mCurrentDepth = 0;
    private int mSupportedDepth = 1;
    private HrefResolver mHrefResolver = null;
    
    public TableOfContents() {
        mNavPoints = new ArrayList<NavPoint>();
    }
    
    /*
     * Unpacks contents from a bundle
     */
    public TableOfContents(Intent intent, String key) {
        mNavPoints = intent.getParcelableArrayListExtra(key);
    }
    
    public void add(NavPoint navPoint) {
        mNavPoints.add(navPoint);
    }
    
    public void clear() {
        mNavPoints.clear();
    }
    
    public NavPoint get(int index) {
        return mNavPoints.get(index);
    }

    /*
     * Used to fetch the last item we're building
     */
    public NavPoint getLatestPoint() {
        return mNavPoints.get(mNavPoints.size() - 1);
    }
    
    public int size() {
        return mNavPoints.size();
    }

    /*
     * Packs this into an intent
     */
    public void pack(Intent intent, String key) {
        intent.putExtra(key, mNavPoints);
    }
    
    /*
     * build parser to parse the "Table of Contents" file,
     * @return parser
     */
    public ContentHandler constructTocFileParser(HrefResolver resolver) {
        RootElement root = new RootElement(XML_NAMESPACE_TABLE_OF_CONTENTS, XML_ELEMENT_NCX);
        Element navMap = root.getChild(XML_NAMESPACE_TABLE_OF_CONTENTS, XML_ELEMENT_NAVMAP);
        Element navPoint = navMap.getChild(XML_NAMESPACE_TABLE_OF_CONTENTS, XML_ELEMENT_NAVPOINT);
        mHrefResolver = resolver;
        AddNavPointToParser(navPoint);
        return root.getContentHandler();
    }

    // Build up code to parse a ToC NavPoint
    private void AddNavPointToParser(final Element navPoint) {
        Element navLabel = navPoint.getChild(XML_NAMESPACE_TABLE_OF_CONTENTS, XML_ELEMENT_NAVLABEL);
        Element text = navLabel.getChild(XML_NAMESPACE_TABLE_OF_CONTENTS, XML_ELEMENT_TEXT);
        Element content = navPoint.getChild(XML_NAMESPACE_TABLE_OF_CONTENTS, XML_ELEMENT_CONTENT);
        
        navPoint.setStartElementListener(new StartElementListener(){
            public void start(Attributes attributes) {
                add(new NavPoint(attributes.getValue(XML_ATTRIBUTE_PLAYORDER)));
                // extend parser to handle another level of nesting if required
                if (mSupportedDepth ==  ++mCurrentDepth) {
                    Element child = navPoint.getChild(XML_NAMESPACE_TABLE_OF_CONTENTS, XML_ELEMENT_NAVPOINT);
                    AddNavPointToParser(child);
                    ++mSupportedDepth;
                }
            }
        });

        text.setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
                getLatestPoint().setNavLabel(body);
            }
        });

        content.setStartElementListener(new StartElementListener(){
            public void start(Attributes attributes) {
                getLatestPoint().setContent(mHrefResolver.ToAbsolute(attributes.getValue(XML_ATTRIBUTE_SCR)));
            }
        });

        navPoint.setEndElementListener(new EndElementListener(){
            public void end() {
                --mCurrentDepth;
            }
        });
    }

}
