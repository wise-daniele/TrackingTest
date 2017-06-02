package com.example.daniele.trackingtest.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by daniele on 30/05/17.
 */

public class Journey {

    @SerializedName("path")
    private ArrayList<Coordinates> path;
    @SerializedName("start")
    private long startTimestamp;
    @SerializedName("end")
    private long endTimestamp;

    public Journey(long start){
        path = new ArrayList<>();
        startTimestamp = start;
    }

    public Journey(long start, LatLng latLng){
        path = new ArrayList<>();
        startTimestamp = start;
        addPoint(latLng);
    }

    public ArrayList<Coordinates> getPath() {
        return path;
    }

    public ArrayList<LatLng> getPathLatLng() {
        ArrayList<LatLng> pathLatLng = new ArrayList<>();
        for(int i = 0; i<path.size(); i++){
            pathLatLng.add(i, new LatLng(path.get(i).getLat(), path.get(i).getLng()));
        }
        return pathLatLng;
    }

    public void setPath(ArrayList<Coordinates> path) {
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

        path.add(0, new Coordinates(point.latitude, point.longitude));
    }

    public void addPoint(Coordinates point){
        path.add(0, point);
    }
}
