package com.dteviot.epubviewer;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class BookmarkDialog {
    private Context mContext;
    private Dialog mDlg;

    public BookmarkDialog(Context context) {
        mContext = context;
    }

    public void setSetBookmarkAction(IAction action) {
        AttachClickListener(R.id.bookmark_dialog_set_button, action);
    }
    
    public void setGotoBookmarkAction(IAction action) {
        AttachClickListener(R.id.bookmark_dialog_goto_button, action);
    }
    
    public void setStartSpeechAction(IAction action) {
        AttachClickListener(R.id.bookmark_dialog_start_speech, action);
    }
    
    public void setStopSpeechAction(IAction action) {
        AttachClickListener(R.id.bookmark_dialog_stop_speech, action);
    }
    
    public void show() {
        mDlg = new Dialog(mContext);
        mDlg.setContentView(R.layout.options_menu);
        mDlg.setTitle(R.string.options_menu_title);
        mDlg.setCancelable(true);
        mDlg.show();
    }

    /*
     * When button clicked, dismiss dialog, then perform action
     */
    private void AttachClickListener(int buttonId, final IAction action) {
        Button button = (Button)(mDlg.findViewById(buttonId));
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                mDlg.dismiss();
                action.doAction();
            }
        });
    }
}
