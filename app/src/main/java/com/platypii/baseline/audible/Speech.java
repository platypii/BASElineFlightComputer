package com.platypii.baseline.audible;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class Speech implements TextToSpeech.OnInitListener {
    private static final String TAG = "Speech";

    private boolean isReady = false;
    private final TextToSpeech tts;

    private List<String> queue;

    public Speech(Context context) {
        tts = new TextToSpeech(context, this);
    }

    void speakNow(String text) {
        if(text != null && text.length() > 0) {
            if(isReady) {
                Log.i(TAG, "Saying: " + text);
                tts.setSpeechRate(MyAudible.getRate());
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                Log.e(TAG, "Speech not ready. Discarding message: " + text);
            }
        }
    }

    void speakWhenReady(String text) {
        if(text != null && text.length() > 0) {
            if(isReady) {
                Log.i(TAG, "Saying when ready: " + text);
                tts.setSpeechRate(MyAudible.getRate());
                tts.speak(text, TextToSpeech.QUEUE_ADD, null);
            } else {
                Log.i(TAG, "Speech not ready. Queueing message: " + text);
                if (queue == null) {
                    queue = new ArrayList<>();
                }
                queue.add(text);
            }
        }
    }

    public void stopAll() {
        if(queue != null) {
            queue.clear();
        }
        tts.stop();
    }

    @Override
    public void onInit(int status) {
        Log.i(TAG, "Text-to-speech is ready");
        isReady = true;

        // Play queued speech
        if(queue != null) {
            for(String text : queue) {
                speakWhenReady(text);
            }
            queue = null;
        }
    }

}
