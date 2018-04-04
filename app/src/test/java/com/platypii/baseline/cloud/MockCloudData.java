package com.platypii.baseline.cloud;

public class MockCloudData extends CloudData {
    public MockCloudData() {
        super(
                "1234",
                System.currentTimeMillis(),
                "2018ish",
                "https://baseline.ws/tracks/1234/track.csv",
                "https://baseline.ws/tracks/1234/track.kml",
                "Norway"
        );
    }
}
