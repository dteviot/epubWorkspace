package com.dteviot.epubviewer.test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.Assert;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xmlpull.v1.XmlSerializer;

import com.dteviot.epubviewer.Globals;
import com.dteviot.epubviewer.MainActivity;
import com.dteviot.epubviewer.ResourceResponse;
import com.dteviot.epubviewer.XmlUtil;
import com.dteviot.epubviewer.XmlFilter.InlineImageElementFilter;
import com.dteviot.epubviewer.XmlFilter.RemoveSvgElementFilter;
import com.dteviot.epubviewer.XmlFilter.StyleSheetElementFilter;
import com.dteviot.epubviewer.XmlFilter.XmlSerializerToXmlFilterAdapter;
import com.dteviot.epubviewer.epub.Book;
import com.dteviot.epubviewer.epub.ManifestItem;

import android.net.Uri;
import android.test.ActivityUnitTestCase;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;

public class PerformanceTest extends ActivityUnitTestCase<MainActivity> {

	public PerformanceTest() {
        super(MainActivity.class);
        // TODO Auto-generated constructor stub
    }

    private ZipFile mZip;
    

    private InputStream fetchFromZip(String fileName) {
        InputStream in = null;
        ZipEntry containerEntry = mZip.getEntry(fileName);
        if (containerEntry != null) {
            try {
                in = mZip.getInputStream(containerEntry);
            } catch (IOException e) {
                Log.e(Globals.TAG, "Error reading zip file " + fileName, e);
            }
        }

        if (in == null) {
            Log.e(Globals.TAG, "Unable to find file in zip: " + fileName);
        }
        
        return new BufferedInputStream(in);
    }

	   private boolean exerciseParser(InputStream in, ContentHandler handler) {
	        try {
	            try {
	                Xml.parse(in, Xml.Encoding.UTF_8, handler);
	                return true;
	            } finally {
	                if (in != null) {
	                    in.close();
	                }
	            }
	        } catch (IOException e) {
	            Log.e(Globals.TAG, "Error reading XML file ", e);
	        } catch (SAXException e) {
	            Log.e(Globals.TAG, "Error parsing XML file ", e);
	        }
	        // if get here, failed
	        return false;
	    }

	/*
	public void testParseManifest() {
        try {
			mZip = new ZipFile("/mnt/sdcard/Download/Northworld_Trilogy.epub");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
        long ave = 0;
        for (int i = 0; i < 10; ++i) {
	        Book book = new Book();
	        InputStream in = fetchFromZip("content.opf");
	        long startTime = System.nanoTime();
	        boolean ok = exerciseParser(in, book.constructOpfFileParser());
	        long elapsedTime = System.nanoTime() - startTime;
	        ave += elapsedTime; 
	        Log.e(Globals.TAG, "Ending Parse, usec = " + ((float)elapsedTime) / 1000);
	        Assert.assertTrue(ok);
	        Assert.assertEquals(155, book.getManifest().getItems().size());
        }
        Log.e(Globals.TAG, "Ending Parse, ave usec = " + ((float)ave) / 10000);
	}
	   
	public void testConvertTitle() throws IOException {
        Book book = new Book("/mnt/sdcard/Download/moby-dick-20120118.epub");
        Assert.assertNotNull(book);
        Uri imageUri = Book.resourceName2Url("OPS/images/9780316000000.jpg");
        Assert.assertNotNull(imageUri);
        int buflen = 4096;
        byte[] buffer = new byte[buflen];
        int offset = 0;

        long ave = 0;
        for (int i = 0; i < 10; ++i) {
	        InputStream in = book.fetch(imageUri).getData(); 
	        Assert.assertNotNull(in);
	        long startTime = System.nanoTime();
	        int len = 0;
	        while (len != -1) {
	            len = in.read(buffer, offset, buffer.length);
	        }
	        long elapsedTime = System.nanoTime() - startTime;
	        in.close();
	        ave += elapsedTime; 
	        Log.e(Globals.TAG, "Loading Bitmap, usec = " + ((float)elapsedTime) / 1000);
        }
        Log.e(Globals.TAG, "Loading Bitmap, ave usec = " + ((float)ave) / 10000);
        
        // to build dataURI
        ave = 0;
        for (int i = 0; i < 10; ++i) {
        	ResourceResponse in = book.fetch(imageUri); 
	        Assert.assertNotNull(in);
	        long startTime = System.nanoTime();
	        XmlUtil.buildDataUri(in);
	        long elapsedTime = System.nanoTime() - startTime;
	        ave += elapsedTime; 
	        Log.e(Globals.TAG, "Building DataURI, usec = " + ((float)elapsedTime) / 1000);
        }
        Log.e(Globals.TAG, "Building DataURI, ave usec = " + ((float)ave) / 10000);
	}
	
	public void testConvertTitleToXml() throws IOException {
        Book book = new Book("/mnt/sdcard/Download/moby-dick-20120118.epub");
        Assert.assertNotNull(book);
        Uri coverUri = Book.resourceName2Url("OPS/cover.xhtml");
        Assert.assertNotNull(coverUri);
        int buflen = 4096;
        byte[] buffer = new byte[buflen];
        int offset = 0;

        long ave = 0;
        for (int i = 0; i < 10; ++i) {
	        long startTime = System.nanoTime();
            // build SAX pipeline to convert XHTML
            // Chain is Reader -> stylesheetFilter -> imageFilter -> Serializer
            XMLFilterImpl stylesheetFilter = new StyleSheetElementFilter(coverUri, book);
            XMLFilterImpl svgFilter = new RemoveSvgElementFilter();
            XMLFilterImpl imageFilter = new InlineImageElementFilter(coverUri, book);
            svgFilter.setParent(stylesheetFilter);
            imageFilter.setParent(svgFilter);

            StringWriter writer = new StringWriter();
            XmlSerializer serializer = android.util.Xml.newSerializer();
            XmlSerializerToXmlFilterAdapter serializerFilter = new XmlSerializerToXmlFilterAdapter(serializer);
            serializerFilter.setParent(imageFilter);
	        long elapsedTime = System.nanoTime() - startTime;
	        Log.e(Globals.TAG, "Setup for XML, usec = " + ((float)elapsedTime) / 1000);
            
            // convert the XHTML
	        startTime = System.nanoTime();
            serializer.setOutput(writer);
            XmlUtil.parseXmlResource(book.fetch(coverUri).getData(), stylesheetFilter,
                    serializerFilter);
            writer.toString();
            elapsedTime = System.nanoTime() - startTime;
	        ave += elapsedTime; 
	        Log.e(Globals.TAG, "Write XML, usec = " + ((float)elapsedTime) / 1000);
        }
        Log.e(Globals.TAG, "Write XML, ave usec = " + ((float)ave) / 10000);
	}
	*/
}
