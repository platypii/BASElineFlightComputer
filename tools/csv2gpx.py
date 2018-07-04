import sys
import gpxpy
import gpxpy.gpx
import datetime
import pdb

# Set to True to begin the track at the maximum altitude, delete everything before.
# Set to False to not delete any data.
BEGIN_AT_MAX_ALT = True
# Column numbers for timestamp, latitude, longitude, and altitude above sea level
TIME_COL = 0
LAT_COL = 4
LON_COL = 5
ALT_COL = 6
# Save to a .gpx file?
SAVE_GPX = False
# Print .gpx file
PRINT_GPX = True

if len(sys.argv) != 2:
	print 'Usage: csv2gpx input.csv'
	exit()

# Write GPX file
with open(sys.argv[1]) as f:
    gpx = gpxpy.gpx.GPX()
    # Create first track in our GPX:
    gpx_track = gpxpy.gpx.GPXTrack()
    gpx.tracks.append(gpx_track)
    # Create first segment in our GPX track:
    gpx_segment = gpxpy.gpx.GPXTrackSegment()
    gpx_track.segments.append(gpx_segment)

    alt = []
	# write point data
    for line in f:
        cols = line.split(',')
        if cols[2] == 'gps':
            # Check if this point is the highest altitude seen so far,
            # If this is the highest point, reinitialize the track
            if BEGIN_AT_MAX_ALT and len(alt) > 1:
                if float(cols[ALT_COL]) > max(alt):
                    alt = [alt[-1]]
                    gpx = gpxpy.gpx.GPX()
                    # Create first track in our GPX:
                    gpx_track = gpxpy.gpx.GPXTrack()
                    gpx.tracks.append(gpx_track)

                    # Create first segment in our GPX track:
                    gpx_segment = gpxpy.gpx.GPXTrackSegment()
                    gpx_track.segments.append(gpx_segment)

            timestamp = datetime.datetime.fromtimestamp(int(cols[TIME_COL])/1000)
            alt.append(float(cols[ALT_COL]))
            gpx_segment.points.append(gpxpy.gpx.GPXTrackPoint(latitude=cols[LAT_COL], longitude=cols[LON_COL], elevation=cols[ALT_COL], time=timestamp))

    # Write the file with the same name as the original
    gpx_file = gpx.to_xml()
    gpx_file_name = sys.argv[1].replace('.csv', '.gpx')
    if SAVE_GPX:
        text_file = open(gpx_file_name, "w")
        text_file.write(gpx_file)
        text_file.close()
    if PRINT_GPX:
        print gpx_file
