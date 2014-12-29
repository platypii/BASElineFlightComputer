package com.platypii.baseline.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;


/**
 * Exports data to the filesystem (KML, CSV, etc)
 * @author platypii
 */
public class Export {
	
	private static boolean cancel = false; // set to true when a user cancels an export
    
    /**
     * Write Flight Data to CSV
     * @param context The application context
     * @param jump The jump to export
     */
    public static void writeCsv(final Context context, final Jump jump) {
    	
    	// Do in background
    	new AsyncTask<Void,Void,Void>() {
    		ProgressDialog progress = null;
			@Override
			protected void onPreExecute() {
		    	// Progress bar
				cancel = false;
				progress = new ProgressDialog(context);
				progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progress.setMax(1);
				progress.setCanceledOnTouchOutside(false);
				progress.setTitle("Export CSV");
		    	progress.setMessage("Saving " + jump.jumpName + ".csv...");
		    	progress.setCancelable(true);
            	progress.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface arg0) {
						cancel = true;
					}
            	});
		    	progress.show();
			}
			@Override
			protected Void doInBackground(Void... arg0) {
		        // Query database
				Cursor cursor = MyDatabase.sensors.queryAll(jump);
		        String filename = jump.jumpName + ".csv";
		        // String filename = "flight-" + DateFormat.format("yyyyMMdd-kkmmss", jump.timeStart) + ".csv";
		        writeCsv(context, filename, cursor, progress);
				return null;
			}
			@Override
            protected void onPostExecute(Void result) {
		        progress.dismiss();
		        if(!cancel)
		        	Toast.makeText(context, "Saved CSV", Toast.LENGTH_SHORT).show();
           }
    	}.execute();
    }
    
    /**
     * Generic method to write Cursor rows to CSV
     * @param context The application context
     * @param filename The name of the file to write to
     * @param cursor The Cursor representing the rows of data
     * @param progress An optional progress dialog to report to
     */
    private static void writeCsv(Context context, String filename, Cursor cursor, ProgressDialog progress) {
        String columns[] = cursor.getColumnNames();
        if(columns.length == 0 || cursor.getCount() == 0)
            return;
        // First row
        OutputStream out = openFile(filename, context);
        try {
            // Read column names from cursor
            // Prepend with human formatted timestamp replacing the _id column
            assert columns[0].equals("_id");
            CharSequence header = "timestamp";
            for(int i = 1; i < columns.length; i++) {
                header = header + "," + columns[i];
            }
            header = header + "\n";
            out.write(header.toString().getBytes());
            if(progress != null) {
            	progress.setMax(cursor.getCount());
            }
            for(int n = 0; cursor.moveToNext() && !cancel; n++) {
            	if(progress != null) {
            		progress.setProgress(n);
            	}
                // Write row
                // Measurement m = new Measurement(cursor);
                assert columns[1].equals("millis");
                long millis = cursor.getLong(1);
                CharSequence str = DateFormat.format("yyyyMMdd-hhmmss", millis) + "." + (millis % 1000);
                for(int i = 1; i < columns.length; i++) {
                    if(cursor.isNull(i)) {
                        str = str + ",";
                    } else {
                        int type = cursor.getType(i);
                        switch(type) {
                            case Cursor.FIELD_TYPE_STRING:
                                str = str + "," + cursor.getString(i);
                                break;
                            case Cursor.FIELD_TYPE_INTEGER:
                                str = str + "," + cursor.getLong(i);
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                str = str + "," + cursor.getDouble(i);
                                break;
                            default:
                                Log.e("Export", "Unknown column type.");
                        }
                    }
                }
                str = str + "\n";
                out.write(str.toString().getBytes());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            Log.e("Export", "Error writing CSV file.\n" + e);
        }
    }
    
    /**
     * Write google-earth KML file
     * @param context The application context
     * @param jump The jump to export
     */
    public static void writeKml(final Context context, final Jump jump) {
    	// Do in background
    	new AsyncTask<Void,Void,Void>() {
    		ProgressDialog progress = null;
			@Override
			protected void onPreExecute() {
		    	// Progress bar
				cancel = false;
				progress = new ProgressDialog(context);
				progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progress.setMax(1);
				progress.setCanceledOnTouchOutside(false);
				progress.setTitle("Export KML");
		    	progress.setMessage("Saving " + jump.jumpName + ".kml...");
		    	progress.setCancelable(true);
            	progress.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface arg0) {
						cancel = true;
					}
            	});
		    	progress.show();
			}
			@Override
			protected Void doInBackground(Void... arg0) {
		        // Query database
		        Cursor cursor = MyDatabase.sensors.queryTrack(jump);
		        if(cursor.getCount() == 0) return null;
		        progress.setMax(cursor.getCount());
		        String filename = jump.jumpName + ".kml";
		        // String filename = "track-" + DateFormat.format("yyyyMMdd-kkmmss", timeStart) + ".kml";
		        OutputStream out = openFile(filename, context);
		        int index_alt = cursor.getColumnIndexOrThrow("altitude");
		        int index_lat = cursor.getColumnIndexOrThrow("latitude");
		        int index_long = cursor.getColumnIndexOrThrow("longitude");
		        try {
		            out.write(kml_header.getBytes());
		            out.write(track_header.getBytes());
		            for(int n = 0; cursor.moveToNext() && !cancel; n++) {
		            	progress.setProgress(n);
		                double altitude = cursor.getDouble(index_alt);
		                double latitude = cursor.getDouble(index_lat);
		                double longitude = cursor.getDouble(index_long);
		                String str = longitude + "," + latitude + "," + altitude + "\n";
		                out.write(str.getBytes());
		                // TODO: Split track at change of flight mode
		            }
		            out.write(track_footer.getBytes());
		            out.write(kml_footer.getBytes());
		            out.flush();
		            out.close();
		        } catch (IOException e) {
		            Log.e("Export", "Error writing KML file.");
		        }
				return null;
			}
			@Override
            protected void onPostExecute(Void result) {
		        progress.dismiss();
		        if(!cancel)
		        	Toast.makeText(context, "Saved KML", Toast.LENGTH_SHORT).show();
           }
    	}.execute();
    }
    
    /**
     * Opens a file for writing
     * @param filename The name of the file to write
     * @param context The application context
     * @return An output stream pointing to the file, else null
     */
    private static OutputStream openFile(String filename, Context context) {
        // Note: could just as easily return a PrintWriter too.
        OutputStream os = null;
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // We can write to external
//            Environment.getExternalFilesDir();
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, filename);
            
            path.mkdirs(); // Make sure the directory exists
            try {
                os = new FileOutputStream(file);
            } catch(FileNotFoundException e) {
                Log.e("Export", "Unable to open external file for writing.");
                return null;
            }
        } else {
            // Internal storage
            if(context != null) {
                try {
                    os = context.openFileOutput(filename, Context.MODE_WORLD_READABLE);
                } catch(FileNotFoundException e) {
                    Log.e("Export", "Unable to open internal file for writing.");
                    return null;
                }
            }
        }
        return os;
    }

    private static final String kml_header   = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                             + "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
                                             + "  <Document>\n"
                                             + "    <Style id=\"ground\">\n" // TODO: Clamp ground to ground?
                                             + "      <LineStyle>\n"
                                             + "        <color>5500dd00</color>\n"
                                             + "        <width>2</width>\n"
                                             + "      </LineStyle>\n"
                                             + "    </Style>\n"
                                             + "    <Style id=\"climb\">\n"
                                             + "      <LineStyle>\n"
                                             + "        <color>55ff0000</color>\n"
                                             + "        <width>2</width>\n"
                                             + "      </LineStyle>\n"
                                             + "    </Style>\n"
                                             + "    <Style id=\"freefall\">\n"
                                             + "      <LineStyle>\n"
                                             + "        <color>ff00ff55</color>\n"
                                             + "        <width>5</width>\n"
                                             + "      </LineStyle>\n"
                                             + "    </Style>\n"
                                             + "    <Style id=\"flight\">\n"
                                             + "      <LineStyle>\n"
                                             + "        <color>ffff5500</color>\n"
                                             + "        <width>5</width>\n"
                                             + "      </LineStyle>\n"
                                             + "    </Style>\n"
                                             + "    <Folder>\n"
                                             + "      <name>Jump</name>\n"
                                             + "      <open>1</open>\n";
    private static final String track_header = "      <Placemark>\n"
                                             + "        <name>Track</name>\n"
                                             + "        <styleUrl>#flight</styleUrl>\n"
                                             + "        <LineString>\n"
                                             + "          <tessellate>1</tessellate>\n"
                                             + "          <altitudeMode>relativeToGround</altitudeMode>\n" // TODO: set absolute ground level
                                             + "          <coordinates>\n";
    private static final String track_footer = "          </coordinates>\n"
                                             + "        </LineString>\n"
                                             + "      </Placemark>\n";
    private static final String kml_footer   = "    </Folder>\n"
                                             + "  </Document>\n"
                                             + "</kml>\n";
    
}
