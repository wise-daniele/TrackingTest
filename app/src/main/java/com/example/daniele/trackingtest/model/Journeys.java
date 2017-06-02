package com.example.daniele.trackingtest.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by daniele on 02/06/17.
 */

public class Journeys {

    @SerializedName("journeys")
    private ArrayList<Journey> journeys;

    public Journeys(){
        journeys = new ArrayList<>();
    }

    public ArrayList<Journey> getJourneys() {
        return journeys;
    }

    public void add(int index, Journey journey){
        journeys.add(index, journey);
    }

}
