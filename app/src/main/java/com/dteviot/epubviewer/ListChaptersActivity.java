package com.dteviot.epubviewer;


import com.dteviot.epubviewer.epub.NavPoint;
import com.dteviot.epubviewer.epub.Book;
import com.dteviot.epubviewer.epub.TableOfContents;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListChaptersActivity extends ListActivity {
    public static final String CHAPTER_EXTRA = "CHAPTER_EXTRA";

    private Book mBook;
    private TableOfContents mToc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBook = new Book(getIntent());
        mToc = mBook.getTableOfContents();
        getListView().setAdapter(new EpubChapterAdapter(this));
    }

    /*
     * Class implementing the "ViewHolder pattern", for better ListView
     * performance
     */
    private static class ViewHolder {
        public TextView textView; // refers to ListView item's TextView
        public NavPoint chapter;
    }

    /*
     * Populates entries on the list
     */
    private class EpubChapterAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public EpubChapterAdapter(Context context) {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder; // holds references to current item's GUI

            // if convertView is null, inflate GUI and create ViewHolder;
            // otherwise, get existing ViewHolder
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.epub_list_item, null);
                viewHolder = new ViewHolder();

                viewHolder.textView = (TextView) convertView.findViewById(R.id.epub_title);
                convertView.setTag(viewHolder); // store as View's tag
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Populate the list item (view) with the chapters details
            viewHolder.chapter = (NavPoint)getItem(position);
            String title = viewHolder.chapter.getNavLabel();
            viewHolder.textView.setText(title);

            return convertView;
        }

        @Override
        public int getCount() {
            return mToc.size();
        }

        @Override
        public Object getItem(int position) {
            return mToc.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // return URI of selected chapter
        Intent intent = new Intent();
        Uri uri = ((ViewHolder)v.getTag()).chapter.getContentWithoutTag(); 
        intent.putExtra(CHAPTER_EXTRA, uri);
        setResult(RESULT_OK, intent);
        finish();
    }    
}
