package com.platypii.baseline.audible;

import com.platypii.baseline.R;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.BaseActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.lang.ref.WeakReference;

/**
 * A background task to check for TTS data
 */
public class CheckTextToSpeechTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "TextToSpeech";

    @NonNull
    private final WeakReference<Activity> activityRef;

    public CheckTextToSpeechTask(Activity activity) {
        this.activityRef = new WeakReference<>(activity);
    }

    @NonNull
    @Override
    protected Boolean doInBackground(Void... voids) {
        final Activity activity = activityRef.get();
        if (activity != null) {
            Log.i(TAG, "Checking for text-to-speech");
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
        } else {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        final Activity activity = activityRef.get();
        if (activity != null && !success) {
            // Let the user know the audible won't be working
            Toast.makeText(activity, R.string.error_audible_not_available, Toast.LENGTH_LONG).show();
        }
    }
}
