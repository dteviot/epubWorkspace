package com.dteviot.epubviewer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import android.net.Uri;
import android.util.Base64;
import android.util.Log;

/*
 * Functions for processing XML
 */
public class XmlUtil {
    private static final int BASE64_DATA_URI = Base64.NO_WRAP;
    

    /*
     * @param uri of the XML document being processed (used to resolve links)
     * @param source to fetch data from
     * @param attrs to update
     * @param attributeName name of attribute to replace value for
     */
    public static AttributesImpl replaceAttributeValueWithDataUri(Uri uri,
            IResourceSource source, 
            Attributes attrs, String attributeName) throws IOException {

        AttributesImpl newAttrs = new AttributesImpl(attrs);

        // find wanted attribute and update
        for(int i = 0; i < newAttrs.getLength(); ++i) {
            if (newAttrs.getLocalName(i).equals(attributeName)) {
                // if it's already a data URI, nothing to do
                String value = newAttrs.getValue(i);
                if ((value.length() < 5) || !value.substring(0, 5).equals("data:")) {
                    Uri content = resolveRelativeUri(uri, value); 
                    ResourceResponse response = source.fetch(content);
                    if (response != null) {
                        newAttrs.setValue(i, buildDataUri(response));
                    }
                }
                break;
            }
        }
        return newAttrs;
    }

    /*
     * Convert a relative URI into an absolute one
     * @param sourceUri of XML document holding the relative URI
     * @param relativeUri to resolve
     * @return absolute URI
     */
    public static Uri resolveRelativeUri(Uri sourceUri, String relativeUri) 
            throws MalformedURLException {
        URL source = new URL(sourceUri.toString());
        URL absolute = new URL(source, relativeUri);
        return Uri.parse(absolute.toString());
    }
    
    public static String buildDataUri(ResourceResponse response)  throws IOException {
        StringBuilder sb = new StringBuilder("data:");
        sb.append(response.getMimeType());
        sb.append(";base64,");
        streamToBase64(response.getData(), sb);
        return sb.toString();
    }

    public static void streamToBase64(InputStream in, StringBuilder sb) throws IOException {
        int buflen = 4096;
        byte[] buffer = new byte[buflen];
        int offset = 0;
        int len = 0;
        while (len != -1) {
            len = in.read(buffer, offset, buffer.length - offset);
            if (len != -1) {
                // must process a multiple of 3 bytes, so that no padding chars 
                // are placed 
                int total = offset + len;
                offset = total % 3; 
                int bytesToProcess = total - offset;
                if (0 < bytesToProcess) {
                    sb.append(Base64.encodeToString(buffer, 0, bytesToProcess, BASE64_DATA_URI));
                }
                // shuffle unused bytes to start of array
                System.arraycopy(buffer, bytesToProcess, buffer, 0, offset);
            } else if (0 < offset) {
                // flush
                sb.append(Base64.encodeToString(buffer, 0, offset, BASE64_DATA_URI));
            }
        }
        in.close();
    }

    /*
     * Parse an XML file in the zip.
     *  @fileName name of XML file in the zip
     *  @root parser to read the XML file
     */
    public static void parseXmlResource(InputStream in, ContentHandler handler, XMLFilterImpl lastFilter) {
        if (in != null) {
            try {
                SAXParserFactory parseFactory = SAXParserFactory.newInstance();
                XMLReader reader = parseFactory.newSAXParser().getXMLReader();
                reader.setContentHandler(handler);

                try {
                    InputSource source = new InputSource(in);
                    source.setEncoding("UTF-8");
                    
                    if (lastFilter != null) {
                        // this is a chain of filters, setup the pipeline
                        ((XMLFilterImpl)handler).setParent(reader);
                        lastFilter.parse(source);
                    } else {
                        // simple content handler
                        reader.parse(source);
                    }
                } finally {
                    in.close();
                }
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(Globals.TAG, "Error reading XML file ", e);
            } catch (SAXException e) {
                Log.e(Globals.TAG, "Error parsing XML file ", e);
            }
        }
    }

    /*
     * @param attrs attributes to look through
     * @param name of attribute to get value of
     * @return value of requested attribute, or null if not found  
     */
    public static String getAttributesValue(Attributes attrs, String name) {
        for(int i = 0; i < attrs.getLength(); ++i) {
            if (attrs.getLocalName(i).equals(name)) {
                return attrs.getValue(i);
            }
        }
        return null;
    }
}
