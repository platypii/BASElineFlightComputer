package com.platypii.baseline.audible;

import com.platypii.baseline.data.MyLocationManager;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


// TODO: Multiple modifiers


public class Event implements Parcelable {

    public String id;
    
    // Enabled
    public boolean enabled;
    
    // Trigger parameters
    public ModeEvent triggerModeEvent; // TODO: regex of flight modes
    public SensorEvent triggerSensorEvent;

    // Sound parameters
    public String sampleName;
    public boolean sampleLoop;

    // Modifier parameters
    public float modifierBalance; // 0..1
    public SensorEvent modifierSensorEvent;
    public boolean modifierFade;
    
    // Current state (not written to database)
    private boolean firing = false;
    private MyAudioTrack track;
    
    
    // Global stuff
    public static final String modes[] = new String[] {
        "Any",
        "Ground",
        "Climb",
        "Freefall",
        "Flight"
    };

    
    public Event(String id) {
        assert id != null;
        this.id = id;
        enabled = false;
        triggerModeEvent = new ModeEvent();
        triggerSensorEvent = new SensorEvent();
        modifierSensorEvent = new SensorEvent();
        modifierBalance = 0.5f;
    }
    public Event(String id, boolean enabled, ModeEvent triggerModeEvent, SensorEvent triggerSensorEvent, 
                 String sampleName, boolean loop, float balance, SensorEvent modifierSensorEvent, boolean fade) {
        assert id != null && !id.equals("");
        this.id = id;
        this.enabled = enabled;
        this.triggerModeEvent = triggerModeEvent==null? new ModeEvent() : triggerModeEvent;
        this.triggerSensorEvent = triggerSensorEvent==null? new SensorEvent() : triggerSensorEvent;
        this.sampleName = sampleName;
        this.sampleLoop = loop;
        this.modifierBalance = balance;
        this.modifierSensorEvent = modifierSensorEvent==null? new SensorEvent() : modifierSensorEvent;
        this.modifierFade = fade;
    }
    /**
     * Copy an event
     */
    public Event(Event copy) {
        this.id = copy.id;
        this.enabled = copy.enabled;
        this.triggerModeEvent = new ModeEvent(copy.triggerModeEvent);
        this.triggerSensorEvent = new SensorEvent(copy.triggerSensorEvent);
        this.sampleName = copy.sampleName;
        this.sampleLoop = copy.sampleLoop;
        this.modifierBalance = copy.modifierBalance;
        this.modifierSensorEvent = new SensorEvent(copy.modifierSensorEvent);
        this.modifierFade = copy.modifierFade;
    }
    /**
     * Create an event from a parcel.
     * @param p The parcel to read from.
     */
    private Event(Parcel p) {
        id = p.readString();
        enabled = p.readInt() == 1;
        // Trigger parameters
        loadTrigger(p.readString());
        // Sound parameters
        loadSound(p.readString());
        // Modifier parameters
        loadModifiers(p.readString());
    }

    /**
     * Create an event from a cursor.
     * @param c The cursor to read from.
     */
    public Event(Cursor c) {
        id = c.getString(0);
        enabled = c.getInt(1) != 0;
        // Trigger parameters
        loadTrigger(c.getString(2));
        // Sound parameters
        loadSound(c.getString(3));
        // Modifier parameters
        loadModifiers(c.getString(4));
    }

    private void loadTrigger(String str) {
    	String split[] = str.split(",");
    	triggerModeEvent = new ModeEvent(split[0]);
    	triggerSensorEvent = new SensorEvent(split[1]);
    }
    private String getTriggerString() {
    	return triggerModeEvent + "," +  triggerSensorEvent;
    }

    private void loadSound(String str) {
    	String split[] = str.split(",");
    	sampleName = split[0];
    	sampleLoop = Boolean.parseBoolean(split[1]);
    }
    private String getSoundString() {
    	return sampleName + "," + sampleLoop;
    }

    private void loadModifiers(String str) {
    	String split[] = str.split(",");
        modifierBalance = Float.parseFloat(split[0]);
    	modifierSensorEvent = new SensorEvent(split[1]);
    	modifierFade = Boolean.parseBoolean(split[2]);
    }
    private String getModifiersString() {
    	return modifierBalance + "," +  modifierSensorEvent + "," + modifierFade;
    }
    
    /**
     * Checks the triggers and updates modifiers
     */
    public void update() {
        // Check trigger
        if(enabled && triggerModeEvent.check() && triggerSensorEvent.check()) {
            // Firing
            if(!firing) {
                assert track == null;
                // Initiate event
                firing = true;
                track = MySoundManager.getTrack(sampleName);
                track.setLoop(sampleLoop? -1 : 0);
                track.setRate(getRate(modifierSensorEvent));
                track.setBalance(modifierBalance);
                if(modifierFade) {
	                long timeSinceLastFix = System.currentTimeMillis() - MyLocationManager.lastFixMillis;
	                float volume = Math.min(1, 1.5f - timeSinceLastFix / 6000f);
	                track.setVolume(volume);
                }
                track.play();
                Log.i("Event", "Firing event: " + this);
            } else {
                // Update event
            	if(!modifierSensorEvent.sensor.getName().equals("None"))
            		track.setRate(getRate(modifierSensorEvent));
                // Fade on signal loss
                if(modifierFade) {
	                long timeSinceLastFix = System.currentTimeMillis() - MyLocationManager.lastFixMillis;
	                float volume = Math.min(1, 1.5f - timeSinceLastFix / 6000f);
	                track.setVolume(volume);
                }
            }
        } else {
            // End firing
            if(firing) {
                // Terminate event
                terminate();
            }
        }
    }

    /**
     * Terminate the event.
     */
    public void terminate() {
        if(firing) {
            firing = false;
            track.stop();
            track = null;
            Log.i("Event", "Terminating event: " + this);
        }
    }
    
    /**
     * Returns the scale factor based on the SensorEvent (min = 0.5, max = 2.0)
     * @param sensorEvent The sensor and range to map from.
     * @return The scale factor based on the SensorEvent.
     */
    private static float getRate(SensorEvent sensorEvent) {
        if(sensorEvent == null || sensorEvent.sensor.getName().equals("None")) {
            return 1;
        } else {
            double value = sensorEvent.sensor.getValue();
            if(Double.isNaN(value)) {
                return 1;
            } else {
                return (float) (0.5 + 1.5 * (value - sensorEvent.min) / (sensorEvent.max - sensorEvent.min));
            }
        }
    }

    // Parcelable stuff
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel p) {
            return new Event(p);
        }
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel p, int flags) {
        p.writeString(id);
        p.writeInt(enabled? 1 : 0);
        // Trigger parameters
        p.writeString(getTriggerString());
        // Sound parameters
        p.writeString(getSoundString());
        // Modifier parameters
        p.writeString(getModifiersString());
    }
    
    /**
     * Returns a ContentValues (database row) representing this Event.
     * @return The ContentValues representing this Event.
     */
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put("_id", id);
        values.put("enabled", enabled? 1:0);
        values.put("trigger", getTriggerString());
        values.put("sound", getSoundString());
        values.put("modifiers", getModifiersString());
        return values;
    }
    
    @Override
    public String toString() {
        return id;
    }
    @Override
    public boolean equals(Object obj) {
        final Event other = (obj instanceof Event)? (Event)(obj) : null;
        return other != null && id.equals(other.id);
    }

}
