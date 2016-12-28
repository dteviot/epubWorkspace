package com.dteviot.epubviewer.test;

import junit.framework.Assert;

import com.dteviot.epubviewer.MainActivity;
import com.dteviot.epubviewer.Utility;

import android.test.ActivityUnitTestCase;

public class UtilityTest extends ActivityUnitTestCase<MainActivity> {

	public UtilityTest() {
        super(MainActivity.class);
        // TODO Auto-generated constructor stub
    }
	
	public void testExtractPath_noParent_returnsEmptyString() {
		String ret = Utility.extractPath("content.opf");
		Assert.assertEquals("", ret);
	}
	
	public void testExtractPath_hasParent_returnsParent() {
		String ret = Utility.extractPath("root/content.opf");
		Assert.assertEquals("root", ret);
	}
	
	public void testConcatPath_basic_returnSuccess() {
		String ret = Utility.concatPath("root" , "content.opf");
		Assert.assertEquals("root/content.opf", ret);
	}

	public void testConcatPath_subdiretory_returnIncludesSubdir() {
		String ret = Utility.concatPath("root" , "dummy/content.opf");
		Assert.assertEquals("root/dummy/content.opf", ret);
	}

	public void testConcatPath_absolute_returnsAbsolute() {
		String ret = Utility.concatPath("root" , "/content.opf");
		Assert.assertEquals("content.opf", ret);
	}

	public void testConcatPath_siblingDiretory_returnsSibling() {
		String ret = Utility.concatPath("root" , "../dummy/content.opf");
		Assert.assertEquals("dummy/content.opf", ret);
	}
}
