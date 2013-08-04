package com.dteviot.epubviewer;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Extracts the Text that would be shown to a user from a XHTML document
 */
public class XhtmlToText extends DefaultHandler{
    /*
     * Nodes that need their contents to be followed by white space
     */
    private static final String[] ADD_WHITE_SPACE_NODES =  
        { "br", "p", "h1", "h2", "h3", "h4", "h5" };

    /*
     * chop text into strings of a couple of hundred words or so. 
     */
    private static final int MIN_CHARS_PER_STRING = 6 * 200;  
    
    private StringBuilder mBuilder;
    private ArrayList<String> mText;
    private boolean mInBody = false;

    public XhtmlToText(ArrayList<String> text) {
        mText = text;
        mBuilder = new StringBuilder();
    } 
    
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);
        // ignore text in head
        if (mInBody) {
            mBuilder.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        super.endElement(uri, localName, name);
        if (isWhiteSpaceNode(localName)) {
            mBuilder.append(" ");
        }
        if (MIN_CHARS_PER_STRING < mBuilder.length()) {
            flushAccumulator();
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        // we're done, make sure any remaining text is moved
        flushAccumulator();
    }

    @Override
    public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);
        if (localName.equalsIgnoreCase("li")) {
            mBuilder.append(" ");
        } else if (localName.equalsIgnoreCase("body")) {
            mInBody = true;
        }
    }

    private void flushAccumulator() {
        if (0 < mBuilder.length()) {
            mText.add(mBuilder.toString());
            mBuilder.setLength(0);
        }
    }
    
    private boolean isWhiteSpaceNode(String nodeName) {
        for (String s : ADD_WHITE_SPACE_NODES) {
            if (s.equals(nodeName)) {
                return true;
            }
        }
        // if get here, not found
        return false;
    }
}
