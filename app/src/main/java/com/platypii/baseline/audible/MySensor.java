package com.platypii.baseline.audible;


/**
 * Represents a sensor (or a function of a sensor)
 */
public abstract class MySensor {
	
	public abstract String getName(); // The name of the sensor

	public abstract double getDefaultMin(); // The default minimum range of the sensor value

	public abstract double getDefaultMax(); // The default maximum range of the sensor value
	
	public abstract double getDefaultStep(); // Round to the nearest step

	/**
	 * The current value of this sensor
	 */
	public abstract double getValue();
	
	public abstract String getUnits();

	/**
	 * Format a value from this sensor into local units (eg- 3.0 (m) -> "10" (ft))
	 */
	public abstract String formatValue(double value);

    /**
     * Parses the input string given in local units, into internal metric value
     * @param string Input in local units (eg- "10" (ft) => 3.1 (m))
     * @return The value in internal units (m, m/s, etc)
     */
	public abstract double parseValue(String input) throws NumberFormatException;

    @Override
	public String toString() {
		return getName();
	}
	
    @Override
	public boolean equals(Object obj) {
    	if(obj == null)
    		return false;
    	else
    		return this.toString().equals(obj.toString());
    }
	
}

