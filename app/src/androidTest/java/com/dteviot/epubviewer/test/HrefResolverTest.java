package com.dteviot.epubviewer.test;

import junit.framework.Assert;

import com.dteviot.epubviewer.MainActivity;
import com.dteviot.epubviewer.HrefResolver;

import android.test.ActivityUnitTestCase;

public class HrefResolverTest extends ActivityUnitTestCase<MainActivity> {

	public HrefResolverTest() {
        super(MainActivity.class);
        // TODO Auto-generated constructor stub
    }

	private static HrefResolver makeResolver() {
		return new HrefResolver("OEPBS/content.opf");
	}

	public void testToAbsolute_nestedPath_returnsFullPath() {
		HrefResolver res = makeResolver();
		String actual = res.ToAbsolute("Text/Afterword.xhtml");
		Assert.assertEquals("OEPBS/Text/Afterword.xhtml", actual);
	}

	public void testToAbsolute_escapedString_returnsUnescapedString() {
		HrefResolver res = makeResolver();
		String actual = res.ToAbsolute("Images/-%20Volume%202%20-%20Page%200%20-%20Insert.jpg");
		Assert.assertEquals("OEPBS/Images/- Volume 2 - Page 0 - Insert.jpg", actual);
	}
}
