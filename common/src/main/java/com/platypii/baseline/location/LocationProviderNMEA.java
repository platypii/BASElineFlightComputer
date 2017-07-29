package com.platypii.baseline.location;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.Numbers;
import android.content.Context;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.util.Log;

class LocationProviderNMEA extends LocationProvider implements GpsStatus.NmeaListener {
    protected final String TAG = "LocationProviderNMEA";
    private static final String NMEA_TAG = "NMEA";

    private final MyAltimeter alti;

    boolean nmeaReceived = false;

    // Most recent data
    private long lastFixMillis = -1;
    private double latitude = Double.NaN;
    private double longitude = Double.NaN;
    private double altitude_gps = Double.NaN;
    private double vN = Double.NaN;
    private double vE = Double.NaN;
    // private double groundSpeed = Double.NaN;
    // private double bearing = Double.NaN;
    private float pdop = Float.NaN;
    private float hdop = Float.NaN;
    private float vdop = Float.NaN;
    private long dateTime; // The number of milliseconds until the start of this day, midnight GMT
    private int gpsFix;

    // Satellite data
    private int satellitesInView = -1;
    private int satellitesUsed = -1;

    // Android Location manager
    private static LocationManager manager;

    @Override
    protected String providerName() {
        return TAG;
    }

    LocationProviderNMEA(MyAltimeter alti) {
        this.alti = alti;
    }

    /**
     * Start location updates
     * @param context The Application context
     */
    @Override
    public synchronized void start(@NonNull Context context) throws SecurityException {
        // Start NMEA updates
        manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        final boolean nmeaSuccess = manager.addNmeaListener(this);
        if (!nmeaSuccess) {
            Log.e(TAG, "Failed to start NMEA updates");
        }
    }

    private void updateLocation() {
        updateLocation(new MLocation(
                lastFixMillis, latitude, longitude, altitude_gps, alti.climb, vN, vE,
                Float.NaN, pdop, hdop, vdop, satellitesUsed, satellitesInView
        ));
    }

    /**
     * This is the main NMEA parsing function.
     * NMEA strings are trimmed, validated, and then parsed into NMEA commands.
     * Location and velocity data is set as NMEA commands arrive.
     * Location is officially updated when we receive the RMC "recommended minimum data" command.
     * @param timestamp milliseconds
     * @param nmea the NMEA string
     */
    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        // Log.v(NMEA_TAG, "[" + timestamp + "] " + nmea.trim()); // Trim because logcat fails on trailing \0

        nmea = cleanNmea(nmea);
        if(nmea.length() < 8) {
            return;
        }

        // Check for missing line breaks
        if(nmea.indexOf('$', 1) > 0) {
            Log.w(TAG, "Splitting multiple NMEA sentences: " + nmea);
            final String[] split = nmea.split("\\$");
            // Recurse on split sentences
            for(String str : split) {
                if(!str.isEmpty()) {
                    onNmeaReceived(timestamp, "$" + str);
                }
            }
        }

        if (!nmeaReceived) {
            Log.d(NMEA_TAG, "First NMEA string received");
            nmeaReceived = true;
        }

        try {
            handleNmea(timestamp, nmea);
        } catch(Exception e) {
            Log.e(NMEA_TAG, "Exception while handling NMEA: " + nmea, e);
            Exceptions.report(new NMEAException("Exception while handling NMEA: " + nmea, e));
        }
    }

    private String cleanNmea(String nmea) {
        // Remove anything before $
        final int sentenceStart = nmea.indexOf('$');
        if(sentenceStart > 0) {
//            Log.w(TAG, "Removing junk before NMEA sentence: " + nmea);
            nmea = nmea.substring(sentenceStart);
        }
        // Trim whitespace and \0
        return nmea.trim();
    }

    private void handleNmea(long timestamp, String nmea) {
        // Validate NMEA sentence and print errors, but still try to parse
        try {
            NMEA.validate(nmea);
        } catch(NMEAException e) {
            Log.e(TAG, "Invalid NMEA sentence", e);
            Exceptions.report(e);
        }

        // Strip checksum
        final int starIndex = nmea.lastIndexOf('*');
        if(0 < starIndex && starIndex < nmea.length()) {
            nmea = nmea.substring(0, starIndex);
        }

        final String split[] = nmea.split(",", -1); // -1 is necessary to preserve trailing columns
        final String command = split[0].substring(3);

        // Parse command
        switch (command) {
            case "GSA":
                // Overall satellite data (DOP and active satellites)
                // boolean autoDim = split[1].equals("A"); // A = Auto 2D/3D, M = Forced 2D/3D
                // gpsFix = split[2].isEmpty() ? 0 : Integer.parseInt(split[2]); // 0 = null, 1 = No fix, 2 = 2D, 3 = 3D
                pdop = Numbers.parseFloat(split[15]);
                hdop = Numbers.parseFloat(split[16]);
                vdop = Numbers.parseFloat(split[17]);
                break;
            case "GSV":
                // Detailed satellite data (satellites in view)
                satellitesInView = Numbers.parseInt(split[3], -1);
                break;
            case "GGA":
                // Fix data
                latitude = NMEA.parseDegreesMinutes(split[2], split[3]);
                longitude = NMEA.parseDegreesMinutes(split[4], split[5]);
                gpsFix = Numbers.parseInt(split[6], -1); // 0 = Invalid, 1 = Valid SPS, 2 = Valid DGPS, 3 = Valid PPS
                satellitesUsed = Numbers.parseInt(split[7], -1);
                hdop = Numbers.parseFloat(split[8]);
                if (!split[9].isEmpty()) {
                    if(!split[10].equals("M")) {
                        Log.e(NMEA_TAG, "Expected meters, was " + split[10] + " in nmea: " + nmea);
                        Exceptions.report(new NMEAException("Expected meters, was " + split[10] + " in nmea: " + nmea));
                    }
                    altitude_gps = Numbers.parseDouble(split[9]);
                }
                // double geoidSeparation = parseDouble(split[11]]); // Geoid separation according to WGS-84 ellipsoid
                // assert split[12].equals("M")// Separation Units
                // double dgpsAge = parseDouble(split[13]);
                // int dgpsStationId = parseDouble(split[14]);
                // TODO: hAcc, vAcc, sAcc
                // Time. At this point we have (at least) 3 choices of clock.
                // 1) timestamp parameter, is measured in milliseconds
                // 2) System.currentTime(), depends on how long execution takes to this point
                // 3) GPS time, most accurate, but must be parsed carefully, since we only get milliseconds since midnight GMT
                // split[1]: Time: 123456 = 12:34:56 UTC
                // if(dateTime == 0 || split[1].isEmpty())
                //   lastFixMillis = timestamp; // Alt: System.currentTimeMillis();
                // else
                //   lastFixMillis = dateTime + parseTime(split[1]);

                break;
            case "RMC":
                // Recommended minimum data for gps
                // This is the NMEA command that we use as the "keyframe" of the NMEA stream.
                // When we receive a valid RMC command, we issue an updateLocation() to listeners.

                // boolean status = split[2].equals("A"); // A = active, V = void
                latitude = NMEA.parseDegreesMinutes(split[3], split[4]);
                longitude = NMEA.parseDegreesMinutes(split[5], split[6]);
                final double groundSpeedRMC = Convert.kts2mps(Numbers.parseDouble(split[7])); // Speed over ground
                final double bearingRMC = Numbers.parseDouble(split[8]); // Course over ground
                // split[10], split[11]: 003.1,W magnetic Variation
                // split[9]: Date: 230394 = 23 March 1994
                // split[1]: Time: 123456 = 12:34:56 UTC
                dateTime = NMEA.parseDate(split[9]);
                lastFixMillis = dateTime + NMEA.parseTime(split[1]);
                // Log.w("Time", "["+timestamp+"] lastFixMillis = " + lastFixMillis + ", currentTime = " + System.currentTimeMillis());
                if(gpsFix == 0) {
                    // Log.v(NMEA_TAG, "Invalid fix, nmea: " + nmea);
                } else if(lastFixMillis <= 0) {
                    Log.w(NMEA_TAG, "Invalid timestamp " + lastFixMillis + ", nmea: " + nmea);
                }

                // Computed parameters
                vN = groundSpeedRMC * Math.cos(Math.toRadians(bearingRMC));
                vE = groundSpeedRMC * Math.sin(Math.toRadians(bearingRMC));

                // Log.i(NMEA_TAG, "["+time+"] " + Convert.latlong(latitude, longitude) + ", groundSpeed = " + Convert.speed(groundSpeed) + ", bearing = " + Convert.bearing2(bearing));

                // Sanity checks
                final int locationError = LocationCheck.validate(latitude, longitude);
                if(locationError != LocationCheck.INVALID_NAN) {
                    if (locationError == LocationCheck.INVALID_ZERO) {
                        // So common we don't even need to report it
                        Log.e(NMEA_TAG, LocationCheck.message[locationError] + ": " + latitude + "," + longitude);
                    } else if (locationError == LocationCheck.INVALID_RANGE) {
                        final String locationErrorMessage = LocationCheck.message[locationError] + ": " + latitude + "," + longitude;
                        Log.e(NMEA_TAG, locationErrorMessage);
                        Exceptions.report(new NMEAException(locationErrorMessage));
                    } else {
                        if (locationError == LocationCheck.UNLIKELY_LAT || locationError == LocationCheck.UNLIKELY_LON) {
                            // Unlikely location, but still update
                            final String locationErrorMessage = LocationCheck.message[locationError] + ": " + latitude + "," + longitude;
                            Log.e(NMEA_TAG, locationErrorMessage);
                            Exceptions.report(new NMEAException(locationErrorMessage));
                        }
                        // Update the official location!
                        updateLocation();
                    }
                }
                break;
            case "GNS":
                // Fixes data for single or combined (GPS, GLONASS, etc) satellite navigation systems
                lastFixMillis = dateTime + NMEA.parseTime(split[1]);
                latitude = NMEA.parseDegreesMinutes(split[2], split[3]);
                longitude = NMEA.parseDegreesMinutes(split[4], split[5]);
                if (!split[7].isEmpty()) {
                    satellitesUsed = Integer.parseInt(split[7]);
                }
                // hdop = Numbers.parseFloat(split[8]);
                if (!split[9].isEmpty()) {
                    altitude_gps = Numbers.parseDouble(split[9]);
                    // double geoidSeparation = parseDouble(split[10]]);
                }
                break;
            case "VTG":
                // final double bearingVTG = Numbers.parseDouble(split[1]); // Course over ground
                // final double groundSpeedVTG = Convert.kts2mps(Numbers.parseDouble(split[5])); // Speed over ground
                break;
            case "GLL":
                // latitude = parseDegreesMinutes(split[1], split[2]);
                // longitude = parseDegreesMinutes(split[3], split[4]);
                // long time = parseUTC(split[5]); // 123456 = 12:34:56 UTC
                // boolean status = split[6].equals("A"); // A = active, V = void
                break;
            case "ACC":
            case "ACCURACY":
                // $GNACCURACY,0.8*1E
            case "AMCLK":
                // Samsung SM-G390F
                // $PSAMCLK,1955,40841247,0,9690613,-2232227,408412467
            case "ATT":
                // $GPATT,45.781233,10.862333,1796.3,45.0,2.6,2.6,*72
                // $GPATT,lat,lon,alt,bear?,???,???
            case "DSTAT":
                // Samsung SM-G955U
                // $AIDSTAT,3,3,3,2,-597,7
            case "GDS":
                // Fly FS451
                // $PCGDS,EPH sow 407586: 1 3 6 9 11 12 14 17 19 22 23 25 31 32--AS:,1995387692*6A
            case "GDV":
                // Fly FS451
                // $PCGDV,Version 0.0 Build 3 Date 13/05/2013 *48
            case "GLG":
                // $GPGLG,VER2,GNSS,130616,171913.0,85,4737.550964,N,12219.566617,W,96.0,38.5,2.0,0.0,13.0,TP,Seoul,,,15,3,,*5C
                // $GPGLG,version,provider,???,???,???,lat,lat,lon,lon,...
            case "EVT":
                // LG-E460
                // $GPEVT,19052017,234044.807,19,,,,
                // split[1]: Date: 19052017 = 19 May 2017
            case "GST":
            case "JNR":
            case "NVD":
            case "ZCD":
                // Rockchip 3GR
                // $GNGST,183132.000,6.1,15,11,76,11,15,32
                // $PGJNR,3,3,09,09,64,11,21
                // $PGNVD,2,1137580,1138164,419442.000,0.612,0.11,-0.01,5,31.0,0,0.0,0,4.6,130,68,15
                // $GNZCD,682.617,+
            case "LOR":
                // Samsung Galaxy S6, S7
                // $PGLOR,0,HLA,123444.00,L,,Al,,A,,H,,,M,1,Ac,0,Gr,0,S,,,Sx,,,T,0,Tr,,Mn,0*33
                // $PGLOR,1,FIX,1.0,1.0*20
                // $PGLOR,2,SIO,TxERR,0,RxERR,0,TxCNT,461,RxCNT,4416,MLFRMPKT,0,DTMS,997,DTIN,5,11,DTOUT,100,987,HATMD,26*20
                // $PGLOR,2,SAT,3,3,G17,033,1F,R24,020,17,G29,026,1F*2F
                // $PGLOR,3,PWR,mA,36.4,RFTm,1000,OscTm,1000,MeasTm,1000,UTC,123444.00*32
                // $PGLOR,6,STA,122301.03,0.000,0.000,-270,236,9999,0,P,F,L,1,C,0,S,0000,0,2,R,0000,TPeF,19,2122,LC,,*13
                // $PGLOR,SPL,20160704142257.8,STATUS,2*38
            case "TK010":
            case "TK011":
                // XGPS-160
                // $PMTK010,002
                // $PMTK011,MTKGPS
            case "TKTSX1":
                // ZVI
                // $PMTKTSX1,181512,0.000,0.293,44.717,4af30000,-3.324382,0.000099,-0.000850,-0.207713,0.386944*40
            case "TIS":
                // Samsung Note7
                // $PSTIS,*61
            case "ZDA":
                // $GPZDA,115729.61,18,06,2016,,*62
            case "PWR":
                // I don't know what PWR does, but we get a lot of them via bluetooth
                break;
            default:
                Log.w(NMEA_TAG, "[" + timestamp + "] Unknown NMEA command: " + nmea);
                Exceptions.report(new NMEAException("Unknown NMEA command " + command + ": " + nmea));
        }
    }

    @Override
    public void stop() {
        super.stop();
        if(manager != null) {
            manager.removeNmeaListener(this);
            manager = null;
        }
    }
}
