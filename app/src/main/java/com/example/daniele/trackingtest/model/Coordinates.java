package com.example.daniele.trackingtest.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by daniele on 02/06/17.
 */

public class Coordinates {

    @SerializedName("lat")
    private double lat;
    @SerializedName("lng")
    private double lng;

    public Coordinates(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
