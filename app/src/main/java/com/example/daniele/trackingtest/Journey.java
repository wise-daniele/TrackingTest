package com.example.daniele.trackingtest;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by daniele on 30/05/17.
 */

public class Journey {

    private ArrayList<LatLng> path;
    private long startTimestamp;
    private long endTimestamp;

    public Journey(long start){
        path = new ArrayList<>();
        startTimestamp = start;
    }

    public ArrayList<LatLng> getPath() {
        return path;
    }

    public void setPath(ArrayList<LatLng> path) {
        this.path = path;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public void addPoint(LatLng point){
        path.add(point);
    }
}
