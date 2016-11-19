package com.platypii.baseline.audible;

/**
 * Describes an audible mode such as horizontal speed, total speed, glide, etc.
 */
public abstract class AudibleMode {

    public final String id;
    public final String name;
    private final String unitsName;

    // Default mode parameters
    public final float defaultMin;
    public final float defaultMax;
    public final int defaultPrecision;

    /**
     * @param id computer readable name of the mode (total_speed)
     * @param name human readable name of the mode (Total Speed)
     * @param unitsName the name of the units (speed)
     * @param defaultMin the default minimum value, in metric
     * @param defaultMax the default maximum value, in metric
     */
    public AudibleMode(String id, String name, String unitsName, float defaultMin, float defaultMax, int defaultPrecision) {
        this.id = id;
        this.name = name;
        this.unitsName = unitsName;
        this.defaultMin = defaultMin;
        this.defaultMax = defaultMax;
        this.defaultPrecision = defaultPrecision;
    }

    public String minimumTitle() {
        return "Minimum " + unitsName;
    }
    public String maximumTitle() {
        return "Maximum " + unitsName;
    }

    /**
     * Convert from local units to internal metric units
     * (local value) * units() = (internal metric value)
     */
    abstract public float units();

    /**
     * Convert from local units to internal metric units
     */
    abstract public String convertOutput(double output, int precision);

}
