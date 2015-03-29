#!/usr/bin/python

import sys

if len(sys.argv) != 2:
	print 'Usage: csv2kml input.csv'
	exit()

kml_header = """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">
  <Document>
    <Style id=\"flight\">
      <LineStyle>
        <color>ffff5500</color>
        <width>5</width>
      </LineStyle>
    </Style>
    <Folder>
      <name>Jump</name>
      <open>1</open>
      <Placemark>
        <name>Track</name>
        <styleUrl>#flight</styleUrl>
        <LineString>
          <tessellate>1</tessellate>
          <altitudeMode>absolute</altitudeMode>
          <coordinates>"""
kml_footer = """
          </coordinates>
        </LineString>
      </Placemark>
    </Folder>
  </Document>
</kml>
"""

# Write KML file
with open(sys.argv[1]) as f:

	# write kml header
	print kml_header

	# write point data
	for line in f:
		cols = line.split(',')
		if cols[1] == 'gps':
			lat = cols[5]
			lon = cols[6]
			alt = cols[7]
			print lon + ',' + lat + ',' + alt

	# write kml footer
	print kml_footer

