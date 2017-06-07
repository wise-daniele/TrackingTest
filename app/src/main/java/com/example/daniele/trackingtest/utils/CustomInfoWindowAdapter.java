package com.example.daniele.trackingtest.utils;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.example.daniele.trackingtest.R;
import com.example.daniele.trackingtest.model.Journey;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/**
 * Created by daniele on 05/06/17.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Journey mJourney;
    private Activity mActivity;
    private final View mMarkerView;

    public CustomInfoWindowAdapter(Activity activity, Journey journey) {
        mActivity = activity;
        mJourney = journey;
        mMarkerView = mActivity.getLayoutInflater()
                .inflate(R.layout.view_custom_info_window, null);
    }

    public View getInfoWindow(Marker marker) {
        render(marker, mMarkerView);
        return mMarkerView;
    }

    public View getInfoContents(Marker marker) {
        return null;
    }

    private void render(Marker marker, View view) {
        ArrayList<LatLng> path = mJourney.getPathLatLng();
        LatLng latestPoint = path.get(0);
        float distance = 0;
        for(int i = path.size()-1; i>=0; i--){
            LatLng currentPoint = path.get(i);
            float currentDistance = Utils.computeDistance(latestPoint, currentPoint);
            distance = distance + currentDistance;
            latestPoint = currentPoint;
        }
        int intDistance = (int)distance;
        long journeyTimeDelta = mJourney.getEndTimestamp() - mJourney.getStartTimestamp();
        double avgSpeed = Utils.computeAvgSpeed(intDistance, journeyTimeDelta);
        TextView textJourneyTime = (TextView) view.findViewById(R.id.text_journey_time);
        TextView textDistanceTo = (TextView) view.findViewById(R.id.text_distance_to);
        TextView textAverageSpeed = (TextView) view.findViewById(R.id.text_average_speed);

        String textMarkerStart = mActivity.getString(R.string.text_start) + " " +
                Utils.getDateFromTimestamp(mJourney.getStartTimestamp());
        String textMarkerEnd = mActivity.getString(R.string.text_end) + " " +
                Utils.getDateFromTimestamp(mJourney.getEndTimestamp());
        String textMarkerDistance = mActivity.getString(R.string.text_path_distance) + " " +
                intDistance + " meters";
        String textAvgSpeed = mActivity.getString(R.string.text_avg_speed) + " " +
                avgSpeed + " Km/h";
        if(marker.getTitle().contains("Start")){
            textJourneyTime.setText(textMarkerStart);
        }
        else{
            textJourneyTime.setText(textMarkerEnd);
        }
        textDistanceTo.setText(textMarkerDistance);
        textAverageSpeed.setText(textAvgSpeed);
    }
}