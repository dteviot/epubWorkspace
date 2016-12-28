package com.dteviot.epubviewer;

import com.dteviot.epubviewer.WebServer.FileRequestHandler;
import com.dteviot.epubviewer.WebServer.ServerSocketThread;
import com.dteviot.epubviewer.WebServer.WebServer;
import com.dteviot.epubviewer.epub.Book;
import com.dteviot.epubviewer.epub.TableOfContents;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements IResourceSource {
    private final static int LIST_EPUB_ACTIVITY_ID = 0; 
    private final static int LIST_CHAPTER_ACTIVITY_ID = 1; 
    private final static int CHECK_TTS_ACTIVITY_ID = 2; 
    
    public static final String BOOKMARK_EXTRA = "BOOKMARK_EXTRA";

    /*
     * the app's main view
     */
    private EpubWebView mEpubWebView;
    
    TextToSpeechWrapper mTtsWrapper;

    private ServerSocketThread mWebServerThread = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);
        // mEpubWebView = (EpubWebView) findViewById(R.id.webview);
        mEpubWebView = createView();
        setContentView(mEpubWebView);
        mTtsWrapper = new TextToSpeechWrapper();
        mWebServerThread = createWebServer();
        mWebServerThread.startThread();
       
        //TestCases.run(this);
        if (savedInstanceState != null) {
            // screen orientation changed, reload
            mEpubWebView.gotoBookmark(new Bookmark(savedInstanceState));
        } else {
            // app has just been started.
            // If a bookmark has been saved, go to it, else, ask user for epub
            // to view
            Bookmark bookmark = new Bookmark(this);
            if (bookmark.isEmpty()) {
                launchBookList();
            } else {
                mEpubWebView.gotoBookmark(bookmark);
            }
        }
    }

    
    private ServerSocketThread createWebServer() {
        FileRequestHandler handler = new FileRequestHandler(this);
        WebServer server = new WebServer(handler);
        return new ServerSocketThread(server, Globals.WEB_SERVER_PORT); 
    }
    
    private EpubWebView createView() {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
            return new EpubWebView23(this); 
        } else {
            return new EpubWebView30(this); 
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_pick_epub:
            launchBookList();
            return true;
        case R.id.menu_bookmark:
            launchBookmarkDialog();
            return true;
        case R.id.menu_chapters:
            launchChaptersList();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void launchBookList() {
        Intent listComicsIntent = new Intent(this, ListEpubActivity.class);
        startActivityForResult(listComicsIntent, LIST_EPUB_ACTIVITY_ID);
    }

    private void launchChaptersList() {
        Book book = getBook(); 
        if (book == null) {
            Utility.showToast(this, R.string.no_book_selected);
        } else {
            TableOfContents toc = book.getTableOfContents();
            if (toc.size() == 0) {
                Utility.showToast(this, R.string.table_of_contents_missing);
            } else {
                Intent listChaptersIntent = new Intent(this, ListChaptersActivity.class);
                toc.pack(listChaptersIntent, ListChaptersActivity.CHAPTERS_EXTRA);
                startActivityForResult(listChaptersIntent, LIST_CHAPTER_ACTIVITY_ID);
            }
        }
    }

    private void launchBookmarkDialog() {
        BookmarkDialog dlg = new BookmarkDialog(this);
        dlg.show();
        dlg.setSetBookmarkAction(mSaveBookmark);
        dlg.setGotoBookmarkAction(mGotoBookmark);
        dlg.setStartSpeechAction(mStartSpeech);
        dlg.setStopSpeechAction(mStopSpeech);
    }

    /*
     * Should return with epub or chapter to load
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHECK_TTS_ACTIVITY_ID) {
            mTtsWrapper.checkTestToSpeechResult(this, resultCode);
            return;
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case LIST_EPUB_ACTIVITY_ID:
                    onListEpubResult(data);
                    break;

                case LIST_CHAPTER_ACTIVITY_ID:
                    onListChapterResult(data);
                    break;
                    
                default:
                    Utility.showToast(this, R.string.something_is_badly_broken);
            }
        } else if (resultCode == RESULT_CANCELED) {
            Utility.showErrorToast(this, data);
        }
    }

    private void onListEpubResult(Intent data) {
        String fileName = data.getStringExtra(ListEpubActivity.FILENAME_EXTRA);
        loadEpub(fileName, null);
    }

    private void onListChapterResult(Intent data) {
        Uri chapterUri = data.getParcelableExtra(ListChaptersActivity.CHAPTER_EXTRA);
        mEpubWebView.loadChapter(chapterUri);
    }

    private void loadEpub(String fileName, Uri chapterUri) {
        mEpubWebView.setBook(fileName);
        mEpubWebView.loadChapter(chapterUri);
    }
    
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        Bookmark bookmark = mEpubWebView.getBookmark();
        if (bookmark != null) {
            bookmark.save(outState);
        }
    }

    private IAction mSaveBookmark = new IAction() {
        public void doAction() {
            Bookmark bookmark = mEpubWebView.getBookmark();
            if (bookmark != null) {
                bookmark.saveToSharedPreferences(MainActivity.this);
            }
        }
    };

    private IAction mGotoBookmark = new IAction() {
        public void doAction() {
            mEpubWebView.gotoBookmark(new Bookmark(MainActivity.this));
        }
    };

    private IAction mStartSpeech = new IAction() {
        public void doAction() {
            mTtsWrapper.checkTextToSpeech(MainActivity.this, CHECK_TTS_ACTIVITY_ID);
            mEpubWebView.speak(mTtsWrapper);
        }
    };
    
    private IAction mStopSpeech = new IAction() {
        public void doAction() {
            mTtsWrapper.stop();
        }
    };
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTtsWrapper.onDestroy();
        mWebServerThread.stopThread();
    }

    /*
     * Book currently being used.
     * (Hack to provide book to WebServer.)
     */
    public Book getBook() { 
        return mEpubWebView.getBook(); 
    }


    @Override
    public ResourceResponse fetch(Uri resourceUri) {
        return getBook().fetch(resourceUri);
    }
}
