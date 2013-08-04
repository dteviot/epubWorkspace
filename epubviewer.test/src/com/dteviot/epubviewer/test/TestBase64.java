package com.dteviot.epubviewer.test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import com.dteviot.epubviewer.MainActivity;
import com.dteviot.epubviewer.XmlUtil;

import android.content.res.AssetManager;
import android.test.ActivityUnitTestCase;
import android.util.Base64;

public class TestBase64 extends ActivityUnitTestCase<MainActivity> {

	public TestBase64() {
        super(MainActivity.class);
        // TODO Auto-generated constructor stub
    }

    public void testBase64encode() {
        try {
            byte[] testData = new byte[64 * 1024 + 7];
            for(int i = 0; i < testData.length; ++i) {
        	    testData[i] = (byte)(i % 256);
            }
        	
            InputStream in = new ByteArrayInputStream(testData);
            StringBuilder sb = new StringBuilder();
            XmlUtil.streamToBase64(in, sb);
            String base64 = sb.toString();
            in.close();
            byte[] raw = Base64.decode(base64, Base64.DEFAULT);
            
            Assert.assertEquals("decoded data wrong length", testData.length, raw.length);

            for (int i = 0; i < raw.length; ++i) {
                Assert.assertEquals("conversion wrong", testData[i], raw[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("testBase64encode failed");
        }
    }

    public void testAssorted() {
        byte[] a1 = { 1 };
        byte[] a2 = { 1, 2 };
        byte[] a3 = { 1, 2, 3 };
        byte[] a4 = { 1, 2, 3, 4 };
        byte[] a5 = { 1, 2, 3, 4, 5 };
        byte[] a6 = { 1, 2, 3, 4, 5, 6 };
        byte[] a7 = { 1, 2, 3, 4, 5, 6, 7 };
        
       testEncode(a1);
       testEncode(a2);
       testEncode(a3);
       testEncode(a4);
       testEncode(a5);
       testEncode(a6);
       testEncode(a7);
       testEncode(new byte[0]);
    }
    
    private static void testEncode(byte[] array) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(array);
            StringBuilder sb = new StringBuilder();
            XmlUtil.streamToBase64(in, sb);
            checkMatch(sb.toString(), array);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("encode failed");
        }
    }

    /*
     * Check that base64 encoded string matches supplied array
     */
    private static void checkMatch(String base64, byte[] array) {
        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
        Assert.assertEquals("lengths don't match", array.length, decoded.length);
        for (int i = 0; i < array.length; ++i) {
            Assert.assertEquals("conversion wrong", array[i], decoded[i]);
        }
    }
}
