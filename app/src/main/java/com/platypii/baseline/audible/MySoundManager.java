package com.platypii.baseline.audible;

import com.platypii.baseline.R;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


// Manages sound output. Modeled after audio editors. There is a database of samples, and 
public class MySoundManager {
    
    private static MySoundManager _instance;
    
    private static AudioManager audioManager;
    private static SoundPool soundPool;
    
    // Maps soundID's to AudioSource
    public static final ArrayList<String> samples = new ArrayList<>(); // list of available samples
    private static final HashMap<String,Integer> sampleMap = new HashMap<>(); // maps sampleName to soundID
    private static final ArrayList<MyAudioTrack> tracks = new ArrayList<>(); // All tracks that have been allocated so far
    
    private static boolean mute = false;

    // Options
    private static final int maxStreams = 10;


    /**
     * Initializes sound services, if not already running
     * 
     * @param appContext The Application context
     */
    static synchronized public void initSounds(Context appContext) {
        if(_instance == null) {
            _instance = new MySoundManager();

            audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
            soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
            soundPool.setOnLoadCompleteListener(soundPoolListener);
    
            // Loads the sound assets
            loadSample(appContext, R.raw.beep1, "Beep 1");
            loadSample(appContext, R.raw.drum1, "Drum Beat 1");
            loadSample(appContext, R.raw.heartbeat, "Heartbeat");
            loadSample(appContext, R.raw.bass1, "Bass Loop 1");
        }
    } 
    
    private static void loadSample(Context context, int resource, String sampleName) {
        sampleMap.put(sampleName, soundPool.load(context, resource, 1));
        samples.add(sampleName);
    }
    
    // Returns the available samples
    public static Set<String> getSamples() {
        return sampleMap.keySet();
    }
    
    // Returns a track object that can be played
    public static MyAudioTrack getTrack(String sampleName) {
        if(sampleMap.containsKey(sampleName)) {
            int soundID = sampleMap.get(sampleName);
            MyAudioTrack track = new MyAudioTrack(soundPool, sampleName, soundID, mute);
            tracks.add(track);
            return track;
        } else {
            Log.e("MySoundManager", "Invalid sample name");
            return null;
        }
    }

    // Mute everything
    public static void setMute(boolean mute) {
        MySoundManager.mute = mute;
        for(MyAudioTrack track : tracks) {
            track.setMute(mute);
        }
    }
    
    public static void cleanup() {
        soundPool.release();
        soundPool = null;
        // Stop streams
        for(MyAudioTrack track : tracks) {
            track.stop();
        }
        sampleMap.clear();
        audioManager.unloadSoundEffects();
        _instance = null;
    }

    // Notify AudioTracks when their sound has loaded. Not ideal, but this is what was given to us by Go*
    private static final SoundPool.OnLoadCompleteListener soundPoolListener = new SoundPool.OnLoadCompleteListener() {
        public void onLoadComplete(SoundPool soundPool, int soundID, int status) {
            // Play sound if looping
            for(MyAudioTrack track : tracks) {
                if(track.soundID == soundID) {
                    track.onLoad();
                }
            }
        }
    };
    

    // Plays a tone
    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    private final int duration = 1; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = duration * sampleRate;

    // Play a tone of the given frequency (in Hz)
    public void playTone(double frequency) {
        final byte generatedSnd[] = new byte[2 * numSamples];

        // Generate tone
        for(int i = 0; i < numSamples; ++i) {
            // generate sample
            double sample = Math.sin(2 * Math.PI * i / (sampleRate/frequency));
            // scale to maximum amplitude
            final short val = (short) ((sample * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[2 * i] = (byte) (val & 0x00ff);
            generatedSnd[2 * i + 1] = (byte) ((val & 0xff00) >>> 8);
        }
        
        final android.media.AudioTrack audioTrack = new android.media.AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples,
                android.media.AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }
    
    
}

