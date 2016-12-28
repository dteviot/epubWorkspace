package com.dteviot.epubviewer;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/*
 * EpubWebView for use with Android 3.0 and above
 * The class uses shouldInterceptRequest() to load
 * resources that are referenced (i.e. Links) into the
 * view.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class EpubWebView30 extends EpubWebView {
    public EpubWebView30(Context context) {
        super(context, null);
    }

    public EpubWebView30(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
     *  Do any Web settings specific to the derived class 
     */
    protected void addWebSettings(WebSettings settings) {
        settings.setDisplayZoomControls(false);
    }
    
    /*
     * @ return Android version appropriate WebViewClient 
     */
    @Override
    protected WebViewClient createWebViewClient() {
        return new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return onRequest(url);
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                onPageLoaded();
            }
        };        
    }
    
    protected void LoadUri(Uri uri) {
        loadUrl(uri.toString());
    }
    
   /*
     * Called when Android 3.0 webview wants to load a URL.
     * @return the requested resource from the ebook
     */
    private WebResourceResponse onRequest(String url) {
        Uri resourceUri = Uri.parse(url);
        WebResourceResponse webResponse = new WebResourceResponse("", "UTF-8", null);  
        ResourceResponse response = getBook().fetch(resourceUri);

        // if don't have resource, give error
        if (response == null) {
            getWebViewClient().onReceivedError(this, WebViewClient.ERROR_FILE_NOT_FOUND,
                    "Unable to find resource in epub", url);
        } else {
            webResponse.setData(response.getData());
            webResponse.setMimeType(response.getMimeType());
        }
        return webResponse;
    }
}
