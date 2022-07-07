package com.platypii.baseline.audible;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.BaseActivity;
import java.util.ArrayList;
import java.util.List;

class Speech implements TextToSpeech.OnInitListener {
    private static final String TAG = "Speech";

    private boolean isReady = false;

    @Nullable
    private TextToSpeech tts;

    @Nullable
    private List<String> queue;

    private int utteranceId = 0;

    public void start(@NonNull Activity activity) {
        Log.i(TAG, "Initializing speech");
        if (!isReady) {
            if (checkTts(activity)) {
                tts = new TextToSpeech(activity.getApplicationContext(), this);
            } else {
                Log.w(TAG, "Text-to-speech not available");
                // Let the user know the audible won't be working
                Toast.makeText(activity, R.string.error_audible_not_available, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(TAG, "Speech started twice");
        }
    }

    public void onTtsLoaded(@NonNull Context context) {
        Log.i(TAG, "TTS loaded");
        tts = new TextToSpeech(context, this);
    }

    private boolean checkTts(@NonNull Activity activity) {
        Log.i(TAG, "Checking for TTS data");
        final Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);

        // Check if intent is supported
        final PackageManager pm = activity.getPackageManager();
        final ResolveInfo resolveInfo = pm.resolveActivity(checkIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null) {
            try {
                activity.startActivityForResult(checkIntent, BaseActivity.RC_TTS_DATA);
                return true;
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Failed to check for TTS package", e);
                Exceptions.report(e);
                return false;
            }
        } else {
            Log.e(TAG, "TTS package not supported");
            return false;
        }
    }

    void speakNow(@Nullable String text) {
        if (text != null && text.length() > 0) {
            if (isReady && tts != null) {
                Log.i(TAG, "Saying: " + text);
                tts.setSpeechRate(Services.audible.settings.speechRate);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utt" + utteranceId++);
            } else {
                Log.e(TAG, "Speech not ready. Discarding message: " + text);
            }
        }
    }

    void speakWhenReady(@Nullable String text) {
        if (text != null && text.length() > 0) {
            if (isReady && tts != null) {
                Log.i(TAG, "Saying when ready: " + text);
                tts.setSpeechRate(Services.audible.settings.speechRate);
                int result = tts.speak(text, TextToSpeech.QUEUE_ADD, null, "utt" + utteranceId++);
                if (result != TextToSpeech.SUCCESS) {
                    Log.w(TAG, "Speech error. Requeueing " + result);
                    if (queue == null) {
                        queue = new ArrayList<>();
                    }
                    queue.add(text);
                }
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
        if (tts != null) {
            tts.stop();
        }
    }

    @Override
    public void onInit(int status) {
        if (isReady) {
            Log.w(TAG, "Text-to-speech is ready twice");
        } else {
            Log.i(TAG, "Text-to-speech is ready");
        }
        isReady = true;

        // Play queued speech
        if (queue != null) {
            while (!queue.isEmpty()) {
                final String text = queue.remove(0);
                speakWhenReady(text);
            }
            queue = null;
        }
    }

}
