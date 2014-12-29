package com.platypii.baseline.audible;


/**
 * Class to represent a sensor and range event.
 */
public class SensorEvent implements EventTrigger {

	public MySensor sensor;
    public double min;
    public double max;

    
    /**
     * Null SensorEvent
     */
    public SensorEvent() {
        this.sensor = MySensors.getSensor("None");
        this.min = 0;
        this.max = 1;
    }
    
    /**
     * New SensorEvent
     */
    public SensorEvent(String name, double min, double max) {
    	if(name == null)
    		this.sensor = MySensors.getSensor("None");
    	else
    		this.sensor = MySensors.getSensor(name);
        this.min = min;
        this.max = max;
    }
    
    /**
     * New SensorEvent from its String representation "sensor min max"
     */
    public SensorEvent(String str) {
    	if(str == null || str.equals("") || str.equals("None")) {
	        this.sensor = MySensors.getSensor("None");
	        this.min = 0;
	        this.max = 1;
    	} else {
	    	String split[] = str.split(" ");
	    	String sensorName = split[0];
	    	for(int i = 1; i < split.length - 2; i++)
	    		sensorName += " " + split[i];
	        this.sensor = MySensors.getSensor(sensorName);
	        this.min = Double.parseDouble(split[split.length - 2]);
	        this.max = Double.parseDouble(split[split.length - 1]);
    	}
    }
    
    /**
     * Copy SensorEvent
     */
    public SensorEvent(SensorEvent copy) {
        this.sensor = copy.sensor;
        this.min = copy.min;
        this.max = copy.max;
    }
    
    public boolean check() {
        // Check sensor
    	String name = sensor.getName();
    	if(name.equals("None")) {
    		return true;
    	} else if(name.equals("Bearing")) {
            double value = sensor.getValue(); 
            if(min <= max)
                return min <= value && value <= max;
            else
                return min <= value || value <= max;
        } else {
            double value = sensor.getValue(); 
            return min <= value && value <= max;
        }
    }

    @Override
    public String toString() {
    	return sensor + " " + min + " " + max;
    }
}
