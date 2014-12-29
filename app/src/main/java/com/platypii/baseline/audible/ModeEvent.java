package com.platypii.baseline.audible;


/**
 * Class to represents a mode event.
 */
public class ModeEvent implements EventTrigger {

	public String mode;

	
	/**
     * Null ModeEvent
     */
    public ModeEvent() {
        this.mode = "Any";
    }

    /**
     * New Mode event
     */
    public ModeEvent(String mode) {
        assert mode != null;
        this.mode = mode==null? "Any" : mode;
    }

	/**
     * Copy ModeEvent
     */
    public ModeEvent(ModeEvent copy) {
    	assert copy.mode != null;
        this.mode = copy.mode;
    }

    public boolean check() {
        return mode.equals("Any") || mode.equals(MyFlightManager.flightMode);
    }

    @Override
    public String toString() {
        return mode;
    }
}
