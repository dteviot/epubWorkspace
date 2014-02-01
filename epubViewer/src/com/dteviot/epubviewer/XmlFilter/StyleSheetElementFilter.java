package com.dteviot.epubviewer.XmlFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import com.dteviot.epubviewer.IResourceSource;
import com.dteviot.epubviewer.ResourceResponse;
import com.dteviot.epubviewer.XmlUtil;

import android.net.Uri;

/*
 * Convert style sheet link elements to in-line style elements 
 */
public class StyleSheetElementFilter extends XMLFilterImpl{
    private static final String LINK_ELEMENT_NAME = "link";
    private static final String STYLE_ELEMENT_NAME = "style";
    private static final String ATTRIBUTE_REL = "rel";
    private static final String ATTRIBUTE_HREF = "href";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_VALUE_TEXT_CSS = "text/css";
    private static final String ATTRIBUTE_URI = "";
    private static final String ATTRIBUTE_CDATA = "CDATA";
    
    private static final String STYLESHEET_REL = "stylesheet";
    
    private IResourceSource mSource;
    private Uri mUri;
    private boolean mReplacingElement = false;
    
    /*
     * @param uri of the XML document being processed (used to resolve links)
     * @param source to fetch data from
     */
    public StyleSheetElementFilter(Uri sourceUri, IResourceSource source) {
        mSource = source;
        mUri = sourceUri;
    }
    
    @Override
    public void startElement(String namespaceURI, String localName, 
            String qualifiedName, Attributes attrs) throws SAXException {
        if (!localName.equals(LINK_ELEMENT_NAME)) {
            super.startElement(namespaceURI, localName, qualifiedName, attrs);
        } else if (isStyleSheet(attrs)) {
            String href = XmlUtil.getAttributesValue(attrs, ATTRIBUTE_HREF);
            String styleSheet = fetchStyleSheet(href);
            if (!styleSheet.isEmpty()) {
                mReplacingElement = true;
                createStyleElement(namespaceURI, styleSheet);
            }
        }
    }
    
    @Override
    public void endElement(String namespaceURI, String localName, 
            String qualifiedName) throws SAXException {
        if (!localName.equals(LINK_ELEMENT_NAME)) {
            super.endElement(namespaceURI, localName, qualifiedName);
        } else if (mReplacingElement) {
            super.endElement(namespaceURI, STYLE_ELEMENT_NAME, STYLE_ELEMENT_NAME);
            mReplacingElement = false;
        }
    }

    private void createStyleElement(String namespaceURI, String styleSheet) 
            throws SAXException {
        AttributesImpl newAttrs = new AttributesImpl();
        newAttrs.addAttribute(ATTRIBUTE_URI, ATTRIBUTE_TYPE, ATTRIBUTE_TYPE,
                ATTRIBUTE_CDATA, ATTRIBUTE_VALUE_TEXT_CSS);
        
        super.startElement(namespaceURI, STYLE_ELEMENT_NAME, 
                STYLE_ELEMENT_NAME, newAttrs);
        
        // now write the style sheet
        char[] text = styleSheet.toCharArray();
        super.characters(text, 0, text.length);
    }
    
    private String fetchStyleSheet(String relativeUri) {
        StringBuffer sb = new StringBuffer();
        try {
            Uri resourceUri = XmlUtil.resolveRelativeUri(mUri, relativeUri);
            ResourceResponse response = mSource.fetch(resourceUri);
            if (response != null)
            {
                try {
                    InputStreamReader isr = 
                            new InputStreamReader(response.getData(), "UTF-8"); 
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        sb.append('\n');
                    }
                } finally {
                    response.getData().close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    
    private boolean isStyleSheet(Attributes attrs) {
    	String relAttribute = XmlUtil.getAttributesValue(attrs, ATTRIBUTE_REL);
        return (relAttribute != null) && 
            (STYLESHEET_REL.compareToIgnoreCase(relAttribute) == 0);
    }
}
