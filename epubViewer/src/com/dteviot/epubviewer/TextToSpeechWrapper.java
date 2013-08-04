package com.dteviot.epubviewer;

import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;

public class TextToSpeechWrapper {
    private TextToSpeech mTts;
    
    private String mText;

    private TextToSpeech.OnUtteranceCompletedListener mCompletedListener; 
    
    
    public void checkTextToSpeech(Activity activity, int activityId) {
        if (mTts == null) {
            // if 4.1 or above, just assume TTS available, as the
            // intents don't work.
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
            {
                Intent checkIntent = new Intent();
                checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                activity.startActivityForResult(checkIntent, activityId);
            } else {
                mTts = new TextToSpeech(activity, mOnInitListener);
            }
        }
    }
    
    public void checkTestToSpeechResult(Context context, int resultCode) {
        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            // success, create the TTS instance
            mTts = new TextToSpeech(context, mOnInitListener);
        } else {
            // missing data, install it
            Utility.showToast(context, R.string.text_to_speech_not_installed);
        }        
    }

    @SuppressWarnings("deprecation")
    public void setOnUtteranceCompletedListener(TextToSpeech.OnUtteranceCompletedListener listener) {
        mCompletedListener = listener;
        if (mTts != null) {
            mTts.setOnUtteranceCompletedListener(listener);
        } 
    }
    
    public void onDestroy() {
        if (mTts != null) {
            mTts.shutdown();
        }
        mTts = null;
    }

    public void speak(String text) {
        mText = text;
        if (mTts != null) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "end");
            mTts.speak(text, TextToSpeech.QUEUE_ADD, params);
        }
    }
    
    public void stop() {
        if (mTts != null) {
            // need to disconnect the listener, otherwise it gets called
            // and will usually feed in more.
            setOnUtteranceCompletedListener(null);
            mTts.stop();
        } 
    }
    
    private TextToSpeech.OnInitListener mOnInitListener = 
            new TextToSpeech.OnInitListener() {

        @SuppressWarnings("deprecation")
        @Override
        public void onInit(int status) {
            if (mTts.isLanguageAvailable(Locale.UK) != 0) {
                mTts.setLanguage(Locale.UK);
            } else {
                mTts.setLanguage(Locale.US);
            }
            mTts.setOnUtteranceCompletedListener(mCompletedListener);
            if (mText != null) {
                speak(mText);
            }
        }
    };

}
