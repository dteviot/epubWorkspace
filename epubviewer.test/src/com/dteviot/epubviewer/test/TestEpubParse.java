package com.dteviot.epubviewer.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import junit.framework.Assert;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.dteviot.epubviewer.Globals;
import com.dteviot.epubviewer.HrefResolver;
import com.dteviot.epubviewer.MainActivity;
import com.dteviot.epubviewer.epub.Book;
import com.dteviot.epubviewer.epub.ManifestItem;
import com.dteviot.epubviewer.epub.TableOfContents;

import android.content.res.AssetManager;
import android.test.ActivityUnitTestCase;
import android.util.Log;
import android.util.Xml;

public class TestEpubParse extends ActivityUnitTestCase<MainActivity> {
	
	public TestEpubParse() {
        super(MainActivity.class);
        // TODO Auto-generated constructor stub
    }

	   @Override  
	   protected void setUp() throws Exception {  
	       super.setUp();  
	    	mAssetManager = getInstrumentation().getContext().getAssets();
	   }  
	   
	   private boolean exerciseParser(String testDataFileName, ContentHandler handler) {
        try {
            InputStream in = mAssetManager.open(testDataFileName);
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

    private AssetManager mAssetManager;    

    public void testFindContainer() {
        String MOBY_DICK_FILENAME = "/mnt/sdcard/Download/moby-dick-20120118.epub";
    	File file = new File(MOBY_DICK_FILENAME);
    	// File can be down loaded from Internet.  Goggle for filename.
    	Assert.assertTrue("moby-dick-20120118 file not on SD card.", file.exists());
        
    	Book book = new Book(MOBY_DICK_FILENAME);
        Assert.assertEquals(".opf not found", "OPS/package.opf", book.getOpfFileName());
    }

    private void setMOpfFileName(Book book)
    {
        try {
            Field field = book.getClass().getDeclaredField("mOpfFileName");
            if(!field.isAccessible()) {
                field.setAccessible(true);
            }        
            field.set(book, "");
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void testParseOpfFile() {
        Book book = new Book();
        
        // set mOpfFileName to not null, or next call will fail.
        setMOpfFileName(book);
        
        exerciseParser("metadata.opf", book.constructOpfFileParser());
        ArrayList<ManifestItem> spine = book.getSpine();
        Assert.assertEquals("toc not found", "ncx", book.getTocID());
        Assert.assertEquals("spine wrong size", 10, spine.size());
        Assert.assertEquals("spine 1st element wrong", "content/calibre_title_page.html", spine.get(0).getHref());
        Assert.assertEquals("spine last element wrong", "content/076543219X_top.htm", spine.get(9).getHref());
        
        ArrayList<ManifestItem> items = book.getManifest().getItems();
        Assert.assertEquals("manifest wrong size", 18, items.size());
        Assert.assertEquals("manifest 1st item id wrong", "id1", items.get(0).getID());
        Assert.assertEquals("manifest 1st item href wrong", "content/calibre_title_page.html", items.get(0).getHref());
        Assert.assertEquals("manifest 1st item media-type wrong", "application/xhtml+xml", items.get(0).getMediaType());
        
        Assert.assertEquals("manifest last item id wrong", "id2", items.get(17).getID());
        Assert.assertEquals("manifest last item href wrong", "content/resources/_cover_.jpg", items.get(17).getHref());
        Assert.assertEquals("manifest last item media-type wrong", "image/jpeg", items.get(17).getMediaType());
    }
    
    public void testParseTocFile() {
        Book book = new Book();
        TableOfContents points = book.getTableOfContents();
        HrefResolver resolver = new HrefResolver("");
        exerciseParser("toc.ncx", points.constructTocFileParser(resolver));
        Assert.assertEquals("toc wrong size", 2, points.size());

        Assert.assertEquals("ToC 1st item playOrder wrong", 1, points.get(0).getPlayOrder());
        Assert.assertEquals("ToC 1st item NavLabel wrong", "Prologue", points.get(0).getNavLabel());
        Assert.assertEquals("ToC 1st item Content wrong", "content/078912345X__p__split_1.html#Chap_0", points.get(0).getContent());
        
        Assert.assertEquals("ToC last item playOrder wrong", 2, points.get(1).getPlayOrder());
        Assert.assertEquals("ToC last item NavLabel wrong", "Lights, Camera, Action.", points.get(1).getNavLabel());
        Assert.assertEquals("ToC last item Content wrong", "content/078912345X__p__split_2.html#Chap_1", points.get(1).getContent());
    }
    
    public void testParseNestedTocFile() {
        TableOfContents toc = new TableOfContents();
        HrefResolver resolver = new HrefResolver("");
        ContentHandler handler = toc.constructTocFileParser(resolver);
        exerciseParser("toc.xml", handler);
        Assert.assertEquals("toc wrong size", 5, toc.size());

        Assert.assertEquals("ToC 1st item navLabel wrong", "Prologue 1", toc.get(0).getNavLabel());
        Assert.assertEquals("ToC 1st item navLabel wrong", "Prologue 1.1", toc.get(1).getNavLabel());
        Assert.assertEquals("ToC 1st item navLabel wrong", "Prologue 1.2", toc.get(2).getNavLabel());
        Assert.assertEquals("ToC 1st item navLabel wrong", "Prologue 1.2.1", toc.get(3).getNavLabel());
        Assert.assertEquals("ToC 2nd item navLabel wrong", "Prologue 2", toc.get(4).getNavLabel());
    }
}
