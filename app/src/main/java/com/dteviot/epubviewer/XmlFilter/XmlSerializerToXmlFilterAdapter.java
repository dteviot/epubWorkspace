package com.dteviot.epubviewer.XmlFilter;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xmlpull.v1.XmlSerializer;

import com.dteviot.epubviewer.Globals;

import android.util.Log;

/*
 * Adapter that converts an XmlSerializer into 
 *  a XmlFilter
 */
public class XmlSerializerToXmlFilterAdapter extends XMLFilterImpl {
    XmlSerializer mSerializer;
 
    public XmlSerializerToXmlFilterAdapter(XmlSerializer serializer) {
        mSerializer = serializer;
    }
    
    @Override
    public void startElement(String namespaceURI, String localName, 
            String qualifiedName, Attributes attrs) throws SAXException {
        super.startElement(namespaceURI, localName, qualifiedName, attrs);
        try {
            mSerializer.startTag(namespaceURI, localName);
            for(int i = 0; i < attrs.getLength(); ++i) {
                mSerializer.attribute(attrs.getURI(i), 
                        attrs.getLocalName(i), attrs.getValue(i));    
            }
        } catch  (IOException e) {
            exceptionHandler(e);
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, 
            String qualifiedName) throws SAXException {
        super.endElement(namespaceURI, localName, qualifiedName);
        try {
            mSerializer.endTag(namespaceURI, localName);
        } catch  (IOException e) {
            exceptionHandler(e);
        }
    }
    
    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        try {
            mSerializer.startDocument("UTF-8", null);
        } catch  (IOException e) {
            exceptionHandler(e);
        }
    }
    
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        try {
            mSerializer.endDocument();
        } catch  (IOException e) {
            exceptionHandler(e);
        }
    }
    
    @Override
    public void characters(char[] text, int start, int length) throws SAXException {
        super.characters(text, start, length);
        try {
            mSerializer.text(text, start, length);
        } catch  (IOException e) {
            exceptionHandler(e);
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        super.startPrefixMapping(prefix, uri);
        try {
           mSerializer.setPrefix(prefix, uri);
        } catch  (IOException e) {
            exceptionHandler(e);
        }
    }

    private static void exceptionHandler(IOException e) {
        Log.e(Globals.TAG, "Error writing XML ", e);
    }
}
