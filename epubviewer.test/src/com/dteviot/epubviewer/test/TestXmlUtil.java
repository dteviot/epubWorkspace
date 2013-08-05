package com.dteviot.epubviewer.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;

import junit.framework.Assert;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xmlpull.v1.XmlSerializer;

import com.dteviot.epubviewer.Globals;
import com.dteviot.epubviewer.IResourceSource;
import com.dteviot.epubviewer.MainActivity;
import com.dteviot.epubviewer.ResourceResponse;
import com.dteviot.epubviewer.XhtmlToText;
import com.dteviot.epubviewer.XmlUtil;
import com.dteviot.epubviewer.XmlFilter.InlineImageElementFilter;
import com.dteviot.epubviewer.XmlFilter.RemoveSvgElementFilter;
import com.dteviot.epubviewer.XmlFilter.StyleSheetElementFilter;
import com.dteviot.epubviewer.XmlFilter.XmlSerializerToXmlFilterAdapter;
import com.dteviot.epubviewer.epub.Book;

import android.content.res.AssetManager;
import android.net.Uri;
import android.test.ActivityUnitTestCase;
import android.util.Log;
import android.util.Xml;

public class TestXmlUtil extends ActivityUnitTestCase<MainActivity> {
	
	public TestXmlUtil() {
        super(MainActivity.class);
        // TODO Auto-generated constructor stub
    }

    public void testStyleSheetElementFilter() {
        AssetManager assetManager = getInstrumentation().getContext().getAssets();
        IResourceSource source = buildSource(assetManager, "file:/stylesheet.css", "stylesheet.css", "text/css");

        String resourceName = "sampleHtml.html";
        Uri uri = Book.resourceName2Url(resourceName);
        XMLFilterImpl stylesheetFilter = new StyleSheetElementFilter(uri, source);
        try {
            TestFilter(stylesheetFilter, assetManager.open(resourceName));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("testStyleSheetElementFilter");
        }
    }

    public void testRemoveSvgImageElementFilter() {
        AssetManager assetManager = getInstrumentation().getContext().getAssets();
        String resourceName = "sampleHtml.html";
        XMLFilterImpl svgFilter = new RemoveSvgElementFilter();
        try {
            TestFilter(svgFilter, assetManager.open(resourceName));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("testStyleSheetElementFilter");
        }
    }

    public void testInlineImageElementFilter() {
        AssetManager assetManager = getInstrumentation().getContext().getAssets();
        IResourceSource source = buildSource(assetManager, "file:/resources/_cover_.jpg", "dummy.bin", "text/css");

        String resourceName = "sampleHtml.html";
        Uri uri = Book.resourceName2Url(resourceName);
        XMLFilterImpl imageFilter = new InlineImageElementFilter(uri, source);
        try {
            TestFilter(imageFilter, assetManager.open(resourceName));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("testStyleSheetElementFilter");
        }
    }

    private static String TestFilter(XMLFilterImpl filter, InputStream in)
            throws IllegalArgumentException, IllegalStateException, IOException {
        // build SAX pipeline to run filter and serialize result
        StringWriter writer = new StringWriter();
        XmlSerializer serializer = android.util.Xml.newSerializer();
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        XmlSerializerToXmlFilterAdapter serializerFilter = 
                new XmlSerializerToXmlFilterAdapter(serializer);
        serializerFilter.setParent(filter);
        
        // convert the XHTML
        serializer.setOutput(writer);
        XmlUtil.parseXmlResource(in, filter, serializerFilter);
        Log.i(Globals.TAG, "Filtered XML=" + writer.toString());
        return writer.toString(); 
    }
    
    private static IResourceSource buildSource(final AssetManager assets, final String nameToLookFor, final String fileName, final String mimeType) {
        return new IResourceSource() {

            @Override
            public ResourceResponse fetch(Uri resourceUri) {
                if (nameToLookFor.equals(resourceUri.toString())) {
                    try {
                        return new ResourceResponse(
                                mimeType, 
                                assets.open(fileName)
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

        };
    }
    
    public void testExtractText() {
        AssetManager assetManager = getInstrumentation().getContext().getAssets();
        ArrayList<String> text = new ArrayList<String>();
        DefaultHandler handler = new XhtmlToText(text);

        InputStream in;
        try {
            in = assetManager.open("chapter_030.xhtml");
            XmlUtil.parseXmlResource(in, handler, null);
            Assert.assertEquals("wrong number of strings", 2, text.size());
            Assert.assertTrue("First string is wrong", text.get(0).startsWith("\n\n\nChapter 30"));
            Assert.assertTrue("Last string is wrong", text.get(1).endsWith("the planks. \n"));
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    public void testBasicXmlSerializer() {
        String sampleXml = 
            "<?xml version='1.0' encoding='UTF-8' ?><html xmlns=\"http://www.w3.org/1999/xhtml\"><head /><body>2005</body></html>";
        
        StringWriter writer = new StringWriter();
        XmlSerializer serializer = Xml.newSerializer(); 
        XmlSerializerToXmlFilterAdapter serializerFilter = 
                new XmlSerializerToXmlFilterAdapter(serializer);
        
        try {
            serializer.setOutput(writer);
            InputStream sb = new ByteArrayInputStream(sampleXml.getBytes("UTF-8"));
            XmlUtil.parseXmlResource(sb, serializerFilter, null);
            Assert.assertEquals("not original string", sampleXml, writer.toString());
        } catch (Exception e) {
            Log.e(Globals.TAG, "Error generating XML ", e);
            Assert.fail();
        }
    }
    
    
    public void testXmlSerializer() {
        String MOBY_DICK_FILENAME = "/mnt/sdcard/Download/moby-dick-20120118.epub";
    	File file = new File(MOBY_DICK_FILENAME);
    	// File can be down loaded from Internet.  Goggle for filename.
    	Assert.assertTrue("moby-dick-20120118 file not on SD card.", file.exists());

    	Book book = new Book(MOBY_DICK_FILENAME);
        StringWriter writer = new StringWriter();
        XmlSerializer serializer = Xml.newSerializer(); 
        XmlSerializerToXmlFilterAdapter serializerFilter = 
                new XmlSerializerToXmlFilterAdapter(serializer);
        
        Uri sourceUri = Book.resourceName2Url("OPS/chapter_030.xhtml");
        XMLFilterImpl linkFilter = new StyleSheetElementFilter(sourceUri, book);
        serializerFilter.setParent(linkFilter);
        try {
            serializer.setOutput(writer);
            XmlUtil.parseXmlResource(book.fetch(sourceUri).getData(), linkFilter, serializerFilter);
            Log.i(Globals.TAG, "Xml Echo = " + writer.toString());
        } catch (Exception e) {
            Log.e(Globals.TAG, "Error generating XML ", e);
            Assert.fail();
        }
    }
}
