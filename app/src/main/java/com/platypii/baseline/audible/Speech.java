package com.platypii.baseline.audible;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

class Speech implements TextToSpeech.OnInitListener {
    private static final String TAG = "Speech";

    private boolean isReady = false;

    @NonNull
    private final TextToSpeech tts;

    @Nullable
    private List<String> queue;

    Speech(Context context) {
        tts = new TextToSpeech(context, this);
    }

    void speakNow(@Nullable String text) {
        if (text != null && text.length() > 0) {
            if (isReady) {
                Log.i(TAG, "Saying: " + text);
                tts.setSpeechRate(AudibleSettings.speechRate);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                Log.e(TAG, "Speech not ready. Discarding message: " + text);
            }
        }
    }

    void speakWhenReady(@Nullable String text) {
        if (text != null && text.length() > 0) {
            if (isReady) {
                Log.i(TAG, "Saying when ready: " + text);
                tts.setSpeechRate(AudibleSettings.speechRate);
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

    void stopAll() {
        if (queue != null) {
            queue.clear();
        }
        tts.stop();
    }

    @Override
    public void onInit(int status) {
        Log.i(TAG, "Text-to-speech is ready");
        isReady = true;

        // Play queued speech
        if (queue != null) {
            for (String text : queue) {
                speakWhenReady(text);
            }
            queue = null;
        }
    }

}
