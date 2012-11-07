package com.platypii.baseline.audible;

import android.util.Log;

import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.data.MySensorEvent;
import com.platypii.baseline.data.MySensorManager;
import com.platypii.baseline.ui.Convert;


/**
 * Defines the sensors (or functions of sensor) made visible to the user.
 * @author platypii
 */
public class MySensors {

	// Sensors list
	private static MySensor sensors[] = null;
	public static MySensor[] getSensors() {
		if(sensors != null) {
			return sensors;
		} else {
			// Initialize sensors
			MySensors _ = new MySensors(); // Scope hack
			sensors = new MySensor[] {
				_.new SensorNull(),
				_.new SensorAltitude(),
				_.new SensorClimbRate(),
				_.new SensorSpeed(),
				_.new SensorGlideAngle(),
				_.new SensorBearing(),
				_.new SensorTilt(),
				_.new SensorDistanceHome(),
				_.new SensorBearingHome()
			};
			return sensors;
		}
	}
	
	/**
	 * Returns the sensor with the given name
	 */
	public static MySensor getSensor(String name) {
		if(name == null || name.equals(""))
			return getSensors()[0];
		for(MySensor sensor : getSensors()) {
			if(sensor.getName().equals(name)) {
				return sensor;
			}
		}
		throw new IllegalArgumentException();
		// return null;
	}

	
	class SensorNull extends MySensor {
		@Override
		public String getName() {
			return "None";
		}
		@Override
		public double getDefaultMin() {
			return 0;
		}
		@Override
		public double getDefaultMax() {
			return 1;
		}
		@Override
		public double getDefaultStep() {
			return 1;
		}
		@Override
		public double getValue() {
			return 0;
		}
		@Override
		public String formatValue(double value) {
			return "";
		}
		@Override
		public double parseValue(String input) throws NumberFormatException {
			return Double.NaN;
		}
		@Override
		public String getUnits() {
			return "";
		}
	}

	class SensorAltitude extends MySensor {
		@Override
		public String getName() {
			return "Altitude";
		}
		@Override
		public double getDefaultMin() {
			return -100 * Convert.FT;
		}
		@Override
		public double getDefaultMax() {
			return 13000 * Convert.FT;
		}
		@Override
		public double getDefaultStep() {
			return 10 * Convert.FT;
		}
		@Override
		public double getValue() {
			return MyAltimeter.altitude;
		}
		@Override
		public String formatValue(double value) {
			return Convert.distance(value, 0, false);
		}
		@Override
		public double parseValue(String input) throws NumberFormatException {
			return Convert.unDistance(Double.parseDouble(input));
		}
		@Override
		public String getUnits() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class SensorClimbRate extends MySensor {
		@Override
		public String getName() {
			return "Climb Rate";
		}
		@Override
		public double getDefaultMin() {
			return -180 * Convert.MPH;
		}
		@Override
		public double getDefaultMax() {
			return 60 * Convert.MPH;
		}
		@Override
		public double getDefaultStep() {
			return 1 * Convert.MPH;
		}
		@Override
		public double getValue() {
			return MyAltimeter.climb;
		}
		@Override
		public String formatValue(double value) {
			return Convert.speed2(value, 0, false);
		}
		@Override
		public double parseValue(String input) throws NumberFormatException {
			return Convert.unSpeed2(Double.parseDouble(input));
		}
		@Override
		public String getUnits() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	class SensorSpeed extends MySensor {
		@Override
		public String getName() {
			return "Speed";
		}
		@Override
		public double getDefaultMin() {
			return 0;
		}
		@Override
		public double getDefaultMax() {
			return 180 * Convert.MPH;
		}
		@Override
		public double getDefaultStep() {
			return 1 * Convert.MPH;
		}
		@Override
		public double getValue() {
			return MyLocationManager.speed;
		}
		@Override
		public String formatValue(double value) {
			return Convert.speed(value, 0, false);
		}
		@Override
		public double parseValue(String input) throws NumberFormatException {
			return Convert.unSpeed(Double.parseDouble(input));
		}
		@Override
		public String getUnits() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class SensorGlideAngle extends MySensor {
		@Override
		public String getName() {
			return "Glide Angle";
		}
		@Override
		public double getDefaultMin() {
			return -90;
		}
		@Override
		public double getDefaultMax() {
			return 90;
		}
		@Override
		public double getDefaultStep() {
			return 1;
		}
		@Override
		public double getValue() {
			return MyLocationManager.glideAngle;
		}
		public String formatValue(double value) {
			return String.format("%.0f", value);
		}
		@Override
		public double parseValue(String input) throws NumberFormatException {
			return Double.parseDouble(input);
		}
		@Override
		public String getUnits() {
			return "°";
		}
	}

	class SensorBearing extends MySensor {
		@Override
		public String getName() {
			return "Bearing";
		}
		@Override
		public double getDefaultMin() {
			return 0;
		}
		@Override
		public double getDefaultMax() {
			return 360;
		}
		@Override
		public double getDefaultStep() {
			return 1;
		}
		@Override
		public double getValue() {
			return MyLocationManager.bearing;
		}
		@Override
		public String formatValue(double value) {
			return String.format("%.0f", value);
		}
		@Override
		public double parseValue(String input) throws NumberFormatException {
			return Double.parseDouble(input);
		}
		@Override
		public String getUnits() {
			return "°";
		}
	}

	class SensorGForce extends MySensor {
		@Override
		public String getName() {
			return "G-Force";
		}
		@Override
		public double getDefaultMin() {
			return 0;
		}
		@Override
		public double getDefaultMax() {
			return 10 * Convert.G;
		}
		@Override
		public double getDefaultStep() {
			return .1;
		}
		@Override
		public double getValue() {
	        if(MySensorManager.accel != null) {
		        MySensorEvent accel = MySensorManager.accel.lastSensorEvent;
            	if(accel != null) {
        			return Math.sqrt(accel.x * accel.x + accel.y * accel.y + accel.z * accel.z);
            	}
            } else {
                Log.e("Event", "Accelerometer not recording!");
            }
            return Double.NaN;
		}
		@Override
		public String formatValue(double value) {
			return Convert.force(value);
		}
		@Override
		public double parseValue(String input) throws NumberFormatException {
			// TODO
			return Double.parseDouble(input);
		}
		@Override
		public String getUnits() {
			return "g";
		}
	}

	class SensorTilt extends MySensor {
		@Override
		public String getName() {
			return "Tilt";
		}
		@Override
		public double getDefaultMin() {
			return -90;
		}
		@Override
		public double getDefaultMax() {
			return 90;
		}
		@Override
		public double getDefaultStep() {
			return 1;
		}
		@Override
		public double getValue() {
	        if(MySensorManager.gravity != null) {
		        MySensorEvent grav = MySensorManager.gravity.lastSensorEvent;
            	if(grav != null) {
                    return Math.toDegrees(Math.atan(grav.y / Math.sqrt(grav.x*grav.x + grav.z*grav.z)));
            	}
            } else {
                Log.e("Event", "Gravity sensor not recording!");
            }
            return Double.NaN;
		}
		@Override
		public String formatValue(double value) {
			return String.format("%.0f", value);
		}
		@Override
		public double parseValue(String input) throws NumberFormatException {
			return Double.parseDouble(input);
		}
		@Override
		public String getUnits() {
			return "°";
		}
	}

	class SensorDistanceHome extends MySensor {
		@Override
		public String getName() {
			return "Distance Home";
		}
		@Override
		public double getDefaultMin() {
			return 0;
		}
		@Override
		public double getDefaultMax() {
			return 5 * Convert.MILES;
		}
		@Override
		public double getDefaultStep() {
			return 10 * Convert.FT;
		}
		@Override
		public double getValue() {
            return MyLocationManager.lastLoc.loc().distanceTo(MyFlightManager.homeLoc);
		}
		@Override
		public String formatValue(double value) {
			return Convert.distance(value, 0, false);
		}
		@Override
		public double parseValue(String input) throws NumberFormatException {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public String getUnits() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class SensorBearingHome extends MySensor {
		@Override
		public String getName() {
			return "Bearing Home";
		}
		@Override
		public double getDefaultMin() {
			return -180;
		}
		@Override
		public double getDefaultMax() {
			return 180;
		}
		@Override
		public double getDefaultStep() {
			return 1;
		}
		@Override
		public double getValue() {
			double theta1 = MyLocationManager.lastLoc.loc().bearingTo(MyFlightManager.homeLoc); // Bearing to home, degrees east of true north
			double theta2 = MyLocationManager.bearing; // Flight bearing
			double theta = theta1 - theta2;
			// Adjust to range (-180,180]
			if(theta <= -180)
				theta += 360;
			return theta;
		}
		@Override
		public String formatValue(double value) {
			return String.format("%.0f", value);
		}
		@Override
		public double parseValue(String input) throws NumberFormatException {
			return Double.parseDouble(input);
		}
		@Override
		public String getUnits() {
			return "°";
		}
	}

}


