# BASEline Flight Computer

[![pipeline status](https://gitlab.com/baselinews/BASElineFlightComputer/badges/master/pipeline.svg)](https://gitlab.com/baselinews/BASElineFlightComputer/pipelines)
[![mit license](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

BASEline Flight Computer is an android app that uses phone sensors to provide audible and visual feedback on speed and position, as well as logging data for later analysis.

https://baseline.ws/

[BASEline on Google Play](https://play.google.com/store/apps/details?id=com.platypii.baseline)

## Logging

Record GPS, altimeter and accelerometer data on your phone.
Upload the data to [baseline.ws](https://baseline.ws/) for later analysis.

## Audible

Without a point of reference in the sky, it can be hard to know how efficiently you are flying your wingsuit.
Audible mode gives realtime in-flight feedback on your speed or glide ratio.
This is a useful tool when learning to maximize your speed and glide ratio, particularly in the skydiving environment.
Audible code including text-to-speech and scripting can be found in the `com.platypii.baseline.audible` package.

## Altimeter

BASEline uses a combination of GPS and the barometric sensor found on many phones to determine altitude.
When logging data to CSV file, we only record the raw barometric pressure.
This preserves the maximum amount of information, since altitude can be recovered in post-processing.
Sensor integration and kalman filtering code can be found in the `com.platypii.baseline.altimeter` package.

## GPS

BASEline has a custom location service that pulls data from multiple sources to get the best estimate of position and speed possible.
BASEline includes its own NMEA parser, so that we can get information not available from the standard android location APIs.
GPS location code can be found in the `com.platypii.baseline.location` package.

## Bluetooth

The best source of GPS data is usually a bluetooth GPS receiver paired with the phone.
BASEline supports connecting directly to any bluetooth GPS receiver that outputs NMEA sentences.
Bluetooth management code can be found in the `com.platypii.baseline.bluetooth` package.
