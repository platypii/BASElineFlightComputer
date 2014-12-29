package com.platypii.baseline.newdata;

import java.util.ArrayList;


/**
 * A class to manage all sensors and dispatch events
 * @author platypii
 */
class EventManager {

	private static final ArrayList<EventListener> altitudeListeners = new ArrayList<>();
	private static final ArrayList<EventListener> locationListeners = new ArrayList<>();
	private static final ArrayList<EventListener> jumpListeners = new ArrayList<>();
	
	
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
