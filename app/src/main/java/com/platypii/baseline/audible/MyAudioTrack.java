package com.platypii.baseline.audible;

import android.media.SoundPool;


// TODO: Only update AudioTrack when there are changes (don't hammer the AudioPool)


/**
 * This class acts like an audio track, and can be played, paused, and rate-changed. 
 * Note, Android doesn't give any way to detect when an AudioPool stream is done playing. 
 * @author platypii
 */
public class MyAudioTrack {
    private final SoundPool soundPool;

    public final String sampleName;
    public final int soundID;
    private int streamID;
    
    private int loop = 0; // -1 = loop, 0 = once, 1 = twice, ...
    private float rate = 1;
    private float volume = 1;
    private float balance = 0.5f; // 0=left, 0.5=center, 1=right
    private float balanceLeft = 1; // balance multiplier for left 
    private float balanceRight = 1; // balance multiplier for right
    private boolean mute = false;
    
    private boolean shouldPlay = false; // true if play() is called before the sound loads. Will play later when onLoad() is called.
    private boolean isLooping = false;


    public MyAudioTrack(SoundPool soundPool, String sampleName, int soundID, boolean mute) {
        this.soundPool = soundPool;
        this.sampleName = sampleName;
        this.soundID = soundID;
        this.mute = mute;
    }
    
    /**
     * Play the track
     */
    public void play() {
        if(!isLooping) {
            // Stop previous playing sound
            if(streamID != 0)
                stop();
            // Start sound playing
            if(mute) {
            	// Zero volume
                streamID = soundPool.play(soundID, 0, 0, 1, loop, rate);
            } else {
                streamID = soundPool.play(soundID, volume * balanceLeft, volume * balanceRight, 1, loop, rate);
            }
            // Check if track started playing (can fail if the resource hasn't loaded yet)
            if(streamID == 0) {
                // Playing failed
                shouldPlay = true;
            } else {
                // Playing succeeded
                if(loop == -1) {
                    isLooping = true;
                }
            }
        } else {
            // Restart track?
        }
    }
    
    /**
     * Sets the loop mode.
     * Beware, this will modify existing streams if already playing.
     * @param loop The number of times to loop: -1 = loop, 0 = once, 1 = twice, ...
     */
    public void setLoop(int loop) {
        assert -1 <= loop;
        this.loop = loop;
        this.isLooping = false; // We don't really know if its looping or not, so assume no.
        if(streamID != 0) {
            // Update looping of currently playing track
            soundPool.setLoop(streamID, loop);
        }
    }
    
    /**
     * Set the playback rate, in the range 0.5..2x
     * @param rate The playback rate. 0.5=half, 2=double speed.
     */
    public void setRate(float rate) {
        // this.rate = Math.max(0.5f, Math.max(rate, 2f));
    	if(this.rate != rate) {
	        this.rate = rate;
	        if(streamID != 0) {
	            // Update rate of currently playing track
	            soundPool.setRate(streamID, rate);
	        }
    	}
    }
    
    /**
     * Set the balance between left and right speakers.
     * @param balance The balance. 0=left, 0.5=center, 1=right
     */
	public void setBalance(float balance) {
		if(this.balance != balance) {
			this.balance = balance;
	    	this.balanceLeft = Math.min(1f, 2f - 2f * balance);
	    	this.balanceRight = Math.min(1f, 2f * balance);
	    	updateVolume();
		}
	}
    
	/**
	 * Set the volume of the track.
	 * @param volume The volume. 0=mute, 1=full.
	 */
	public void setVolume(float volume) {
		if(this.volume != volume) {
			this.volume = volume;
			updateVolume();
		}
	}

	/**
	 * Mute or unmute the track.
	 * @param mute The mute state.
	 */
    public void setMute(boolean mute) {
        this.mute = mute;
        updateVolume();
    }
    
    /**
     * Updates the track volume based on mute, volume and balance.
     */
    private void updateVolume() {
        if(streamID != 0) {
        	if(mute) {
        		soundPool.setVolume(streamID, 0, 0);
        	} else {
        		soundPool.setVolume(streamID, volume * balanceLeft, volume * balanceRight);
        	}
        }
    }
    
    // Stops the track
    public void stop() {
        if(streamID != 0) {
            soundPool.stop(streamID);
            streamID = 0;
            isLooping = false;
            shouldPlay = false;
        }
    }
    
    // Should be called when the audio resource is loaded, if it wasn't at creation
    public void onLoad() {
        if(shouldPlay) {
            play();
            shouldPlay = false;
        }
    }

}
