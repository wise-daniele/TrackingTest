package com.example.daniele.trackingtest;

import com.google.android.gms.location.LocationRequest;

/**
 * Created by daniele on 30/05/17.
 */

public class Constants {

    public static final int REQUEST_CHECK_SETTINGS = 100;

    //Location Request Values
    public static final int LOCATION_REQUEST_ACCURACY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    public static final int LOCATION_REQUEST_INTERVAL = 8*1000;
    public static final int LOCATION_REQUEST_FASTEST_INTERVAL = 5*1000;

    //Fragmemts Tags
    public static final String MAP_FRAGMENT_TAG = "fragment_map";
    public static final String JOURNEYS_FRAGMENT_TAG = "fragment_journeys";
}
