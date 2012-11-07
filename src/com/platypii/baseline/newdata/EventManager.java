package com.platypii.baseline.newdata;

import java.util.ArrayList;


/**
 * A class to manage all sensors and dispatch events
 * @author platypii
 */
public class EventManager {

	private static ArrayList<EventListener> altitudeListeners = new ArrayList<EventListener>();
	private static ArrayList<EventListener> locationListeners = new ArrayList<EventListener>();
	private static ArrayList<EventListener> jumpListeners = new ArrayList<EventListener>();
	
	
	public static void registerListener(EventListener listen, int type) {
		if(type == Event.TYPE_ALTITUDE) {
			altitudeListeners.add(listen);
		} else if(type == Event.TYPE_LOCATION) {
			locationListeners.add(listen);
		} else if(type == Event.TYPE_JUMP) {
			jumpListeners.add(listen);
		}
	}

}
