package com.platypii.baseline.audible;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

class Speech implements TextToSpeech.OnInitListener {
    private static final String TAG = "Speech";

    private boolean isReady = false;
    private TextToSpeech tts;

    public Speech(Context context) {
        tts = new TextToSpeech(context, this);
        tts.setLanguage(Locale.US);
    }

    void speakNow(String text) {
        if(isReady) {
            Log.i(TAG, "Saying " + text);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            Log.e(TAG, "Speech not ready. Discarding message: " + text);
        }
    }

    @Override
    public void onInit(int status) {
        Log.i(TAG, "Text-to-speech is ready");
        isReady = true;
    }

}
