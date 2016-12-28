package com.dteviot.epubviewer.test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.dteviot.epubviewer.Globals;
import com.dteviot.epubviewer.MainActivity;

import android.test.ActivityUnitTestCase;
import android.util.Log;
import android.webkit.WebView;

public class ReflectionTest extends ActivityUnitTestCase<MainActivity> {

	public ReflectionTest() {
        super(MainActivity.class);
        // TODO Auto-generated constructor stub
    }

    public static void getScreenText(WebView webView) {
        try {
            // get the webViewProvider
            Method getWebViewProvider = webView.getClass().getMethod("getWebViewProvider", null);
            
            Object o = getWebViewProvider.invoke(webView, (Object[])null);
                
            // o should be a WebViewClassic,
            // from this we want to get to WebViewCore
            Method getWebViewCore = o.getClass().getMethod("getWebViewCore", null);

            o = getWebViewCore.invoke(o, (Object[])null);
            Method nativeGetText = o.getClass().getDeclaredMethod("nativeGetText", 
                    new Class[] {int.class, int.class, int.class, int.class, int.class});
            if (!nativeGetText.isAccessible()) {
                nativeGetText.setAccessible(true);
            }        
            
            // dumpMethod("nativeGetText", o.getClass().getDeclaredMethods());
            
            // 
            int nativeClass = fetchPrivateInt("mNativeClass", o);
            Log.e(Globals.TAG, "nativeClass = " + nativeClass);
            
            int mViewportWidth = fetchPrivateInt("mViewportWidth", o);
            Log.e(Globals.TAG, "mViewportWidth = " + mViewportWidth);
            
            int mViewportHeight = fetchPrivateInt("mViewportHeight", o);
            Log.e(Globals.TAG, "mViewportWidth = " + mViewportWidth);

            // try getting text from screen
            int top = webView.getScrollY();
            int left = webView.getScrollX();
            int bottom = top + webView.getHeight();
            int right = left + webView.getWidth();
            Object[] args = { nativeClass, left, top, right, bottom }; 
            Object s = nativeGetText.invoke(o, args);
            Log.e(Globals.TAG, "nativeGetText() = " + s);
            
        
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }
    
    private static int fetchPrivateInt(String name, Object obj) 
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(name);
        if(!field.isAccessible()) {
            field.setAccessible(true);
        }        
        return field.getInt(obj);
    }
    
    
    public static void dumpClass(Object o) {
        Class c = o.getClass();
        String name = c.getName();
        Log.e(Globals.TAG, "Class = " + name);
        
        dumpMembers(c.getDeclaredMethods()); 
        dumpMembers(c.getMethods()); 
    }
    
    public static void dumpMembers(Method[] methods) {
        for(int i = 0; i < methods.length; ++i) {
            String name = methods[i].getName();
            Log.e(Globals.TAG, "Method " + name);
            dumpMethod(methods[i]);
        }
    }

    public static void dumpMethod(String name, Method[] methods) {
        for(int i = 0; i < methods.length; ++i) {
            String n = methods[i].getName();
            if (name.equals(n)) {
                Log.e(Globals.TAG, "Method " + name);
                dumpMethod(methods[i]);
            }
        }
    }
    
    public static void dumpMethod(Method m) {
        Class[] params = m.getParameterTypes();
        for (int j = 0; j < params.length; ++j) {
            String name = params[j].getName();
            Log.e(Globals.TAG, "Param " + name);
        }
    }
}
