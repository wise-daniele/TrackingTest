package com.example.daniele.trackingtest;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.example.daniele.trackingtest.model.Journeys;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


/**
 * Created by daniele on 31/05/17.
 */

public class Utils {

    private static final String DB_PREFERENCE = "db_preference";
    private static final String JOURNEYS = "journeys";

    public static Journeys getJourneys(Context context){
        SharedPreferences prefs = context.getSharedPreferences(DB_PREFERENCE, Context.MODE_PRIVATE);
        String json = prefs.getString(JOURNEYS, null);
        Gson gson = new Gson();
        return gson.fromJson(json, Journeys.class);
    }

    /**
     * Save journeys as a json String. This approach is better than using SQLite for various reasons
     * First of all it is faster as there's no need to query a database. Then, in this demo we need
     * the journeys object as a whole without the need to query a specific item within the application.
     * Last but not least the journeys are ready to be sent to a server as a json object.
     * @param context Context
     * @param journeys list of journeys to save
     */
    public static void setJourneys(Context context, Journeys journeys){
        Gson gson = new Gson();
        String json = gson.toJson(journeys);
        SharedPreferences prefs = context.getSharedPreferences(DB_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(JOURNEYS, json);
        editor.apply();
    }


    /**
     * Returns a string representing the date of the timestamp given as parameter
     * @param timestamp unix timestamp
     * @return date related to timestamp
     */
    public static String getDateFromTimestamp(long timestamp){
        Calendar mydate = Calendar.getInstance();
        mydate.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW, Locale.getDefault());
        return sdf.format(mydate.getTime());
    }

    /**
     * Computes the distance between two latlng points
     * @return
     */
    public static float computeDistance(LatLng pointA, LatLng pointB){
        Location locA = new Location("");
        locA.setLatitude(pointA.latitude);
        locA.setLongitude(pointA.longitude);
        Location locB = new Location("");
        locB.setLatitude(pointB.latitude);
        locB.setLongitude(pointB.longitude);
        return locA.distanceTo(locB);
    }

    /**
     * Computes the average speed of the journey
     * @param space Space covered (in meters)
     * @param timeDelta Time elapsed (in milliseconds)
     * @return The average speed in Km/h
     */
    public static float computeAvgSpeed(int space, long timeDelta){
        space = space / 1000;
        float time = (timeDelta / (float)1000) / ((float)(60 * 60));
        return ((float)space)/time;
    }
}
