package com.dteviot.epubviewer.XmlFilter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;


/*
 * remove <svg> elements, and convert <image> elements
 * into <img> elements
 */
public class RemoveSvgElementFilter extends XMLFilterImpl {
    private static final String SVG_ELEMENT_NAME = "svg";
    private static final String IMG_ELEMENT_NAME = "img";
    private static final String IMAGE_ELEMENT_NAME = "image";
    
    private static final String ATTRIBUTE_HREF = "href";
    private static final String ATTRIBUTE_SRC = "src";
    private static final String DEFAULT_URI = ""; 
    private static final String IMG_NAMESPACE = "http://www.w3.org/1999/xhtml";
    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
    private static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink"; 

    private boolean mRemovingSvgElement = false;
    
    /*
     * @param uri of the XML document being processed (used to resolve links)
     * @param source to fetch data from
     */
    public RemoveSvgElementFilter() {
    }
    
    @Override
    public void startElement(String namespaceURI, String localName, 
            String qualifiedName, Attributes attrs) throws SAXException {
        if (localName.equals(SVG_ELEMENT_NAME)) {
            // just delete the SVG elements
            mRemovingSvgElement = true;
            return;
        } else if (localName.equals(IMAGE_ELEMENT_NAME) && mRemovingSvgElement) {
            // replace <image> with <img>
            convertImageToImgElement(attrs);
            return;
        }
        super.startElement(namespaceURI, localName, qualifiedName, attrs);
    }

    @Override
    public void endElement(String namespaceURI, String localName, 
            String qualifiedName) throws SAXException {
        if (localName.equals(SVG_ELEMENT_NAME)) {
            // just delete the SVG elements
            mRemovingSvgElement = false;
            return;
        } else if (localName.equals(IMAGE_ELEMENT_NAME) && mRemovingSvgElement) {
            super.endElement(IMG_NAMESPACE, IMG_ELEMENT_NAME, IMG_ELEMENT_NAME);
            return;
        }
        super.endElement(namespaceURI, localName, qualifiedName);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // prune out namespaces used with SVG
        if (!uri.equals(SVG_NAMESPACE) && !uri.equals(XLINK_NAMESPACE)) {
            super.startPrefixMapping(prefix, uri);
        }
    }
    
    
    private void convertImageToImgElement(Attributes attrs) 
            throws SAXException {
        AttributesImpl newAttrs = new AttributesImpl(attrs);;
        
        // put all attributes in default namespace and
        // rename "xlink:href" attribute to "src"
        for(int i = 0; i < newAttrs.getLength(); ++i) {
            newAttrs.setURI(i, DEFAULT_URI);
            String localName = newAttrs.getLocalName(i);  
            if (ATTRIBUTE_HREF.equals(localName)) {
                newAttrs.setLocalName(i, ATTRIBUTE_SRC);
                newAttrs.setQName(i, ATTRIBUTE_SRC);
            }
        }
        
        super.startElement(IMG_NAMESPACE, IMG_ELEMENT_NAME, 
                IMG_ELEMENT_NAME, newAttrs);
    }
}
