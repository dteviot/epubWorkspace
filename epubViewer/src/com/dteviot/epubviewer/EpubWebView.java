package com.dteviot.epubviewer;

import java.util.ArrayList;

import com.dteviot.epubviewer.epub.Book;

import android.content.Context;
import android.graphics.Picture;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/*
 * Holds the logic for the App's 
 * special WebView handling
 */
public abstract class EpubWebView extends WebView {
    private final static float FLING_THRESHOLD_VELOCITY = 200;

    /*
     * The book view will show
     */
    private Book mBook;
    
    private GestureDetector mGestureDetector;
    
    /*
     * "root" page we're currently showing
     */
    private Uri mCurrentResourceUri;
    
    /*
     * Position of document
     */
    private float mScrollY = 0.0f; 

    /*
     * Note that we're loading from a bookmark
     */
    private boolean mScrollWhenPageLoaded = false;
    
    /*
     * To speak the text
     */
    private TextToSpeechWrapper mTtsWrapper;
    
    /*
     * The page, as text
     */
    private ArrayList<String> mText;

    private WebViewClient mWebViewClient;
    
    /*
     * Current line being spoken
     */
    private int mTextLine;

    /*
     * Pick an initial default
     */
    private float mFlingMinDistance = 320;
    
    public EpubWebView(Context context) {
        this(context, null);
    }

    public EpubWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, mGestureListener);
        WebSettings settings = getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        settings.setBuiltInZoomControls(true);
        setWebViewClient(mWebViewClient = createWebViewClient());
        setWebChromeClient(new WebChromeClient());
    }

    /*
     * @ return Android version appropriate WebViewClient 
     */
    abstract protected WebViewClient createWebViewClient();
    
    /*
     * Book to show
     */
    public void setBook(String fileName) {
        // if book already loaded, don't load again
        if ((mBook == null) || !mBook.getFileName().equals(fileName)) {
            mBook = new Book(fileName);
        }
    }

    public Book getBook() {
        return mBook;
    }
    
    protected WebViewClient getWebViewClient() {
        return mWebViewClient;
    }
    
    /*
     * Chapter of book to show
     */
    public void loadChapter(Uri resourceUri) {
        if (mBook != null) {
            // if no chapter resourceName supplied, default to first one.
            if (resourceUri == null) {
                resourceUri = mBook.firstChapter();
            }
            if (resourceUri != null) {
                mCurrentResourceUri = resourceUri;
                // prevent cache, because WebSettings.LOAD_NO_CACHE doesn't always work.
                clearCache(false);
                LoadUri(resourceUri);
            }
        }
    }

    /*
     * @ return load contents of URI into WebView,
     *   implementation is android version dependent 
     */
    protected abstract void LoadUri(Uri uri);
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event) ||
             super.onTouchEvent(event);
    }

    GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            // if no book, nothing to do
            if (mBook == null) {
                return false;
            }
            
            // also ignore swipes that are vertical, too slow, or too short.
            float deltaX = event2.getX() - event1.getX();
            float deltaY = event2.getY() - event1.getY();
            
            if ((Math.abs(deltaX) < Math.abs(deltaY))
                    || (Math.abs(deltaX) < mFlingMinDistance)
                    || (Math.abs(velocityX) < FLING_THRESHOLD_VELOCITY)) {
                return false;
            } else {
                if (deltaX < 0) {
                    return changeChapter(mBook.nextResource(mCurrentResourceUri));
                } else {
                    return changeChapter(mBook.previousResource(mCurrentResourceUri));
                }
            }
        }
        
        // Useful on simulator where swipe dodgy.  Less so on real devices.
        /*
        @Override
        public boolean onDoubleTap (MotionEvent e) {
            return changeChapter(mBook.nextResource(mCurrentResourceUri));
        }
        */
    };

    private boolean changeChapter(Uri resourceUri) {
        if (resourceUri != null) {
            loadChapter(resourceUri);
            return true;
        } else {
            Utility.showToast(getContext(), R.string.end_of_book);
            return false;
        }
    }
    
    /*
     * Store current view into bookmark
     */
    public Bookmark getBookmark() {
        if ((mBook != null) && (mCurrentResourceUri != null)) {
            float contentHeight = (float)getContentHeight();
            contentHeight = (contentHeight == 0.0f) ? 0.0f : getScrollY() / contentHeight; 
            return new Bookmark (
                mBook.getFileName(),
                mCurrentResourceUri,
                contentHeight
            );
        }
        return null;
    }

    public void gotoBookmark(Bookmark bookmark) {
        if (!bookmark.isEmpty()) {
            mScrollY = bookmark.mScrollY;

            // get notify when content height is available, for setting Y scroll position
            mScrollWhenPageLoaded = true;
            setBook(bookmark.getFileName());
            loadChapter(bookmark.getResourceUri());
        }
    }

    public void speak(TextToSpeechWrapper ttsWrapper) {
        mTtsWrapper = ttsWrapper;
        if ((mBook != null) && (mCurrentResourceUri != null)) {
            mText = new ArrayList<String>();
            XmlUtil.parseXmlResource(
                    mBook.fetch(mCurrentResourceUri).getData(), 
                    new XhtmlToText(mText), null);
            mTextLine = 0;
            mTtsWrapper.setOnUtteranceCompletedListener(mCompletedListener);
            if (0 < mText.size()) {
                mTtsWrapper.speak(mText.get(0));
            }
        }
    }

    /*
     * Send next piece of text to Text to speech
     */
    private TextToSpeech.OnUtteranceCompletedListener mCompletedListener = 
            new TextToSpeech.OnUtteranceCompletedListener() {

        @Override
        public void onUtteranceCompleted(String utteranceId) {
            if (mTextLine < mText.size() - 1) {
                mTtsWrapper.speak(mText.get(++mTextLine));
            }
        }
        
    };

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mFlingMinDistance = w / 2;
    }

    /*
     * Called when page is loaded,
     * if we're scrolling to a bookmark, we need to set the
     * page size listener here.  Otherwise it can be called
     * for pages other than the one we're interested in 
     */
    @SuppressWarnings("deprecation")
    protected void onPageLoaded() {
        if (mScrollWhenPageLoaded) {
            setPictureListener(mPictureListener);
            mScrollWhenPageLoaded = false;
        }
    }
    
    /*
     * Need to wait until view has figured out how big web page is
     * Otherwise, we can't scroll to last position because 
     * getContentHeight() returns 0.
     * At current time, there is no replacement for PictureListener 
     */
    @SuppressWarnings("deprecation")
    private PictureListener mPictureListener = new PictureListener() {
        @Override
        @Deprecated
        public void onNewPicture(WebView view, Picture picture) {
            // stop listening 
            setPictureListener(null);
            
            scrollTo(0, (int)(getContentHeight() * mScrollY));
            mScrollY = 0.0f;
        }
    };
}
