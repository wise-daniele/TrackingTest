package com.example.daniele.trackingtest.controller;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.daniele.trackingtest.Constants;
import com.example.daniele.trackingtest.Journey;
import com.example.daniele.trackingtest.ui.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by daniele on 29/05/17.
 */

public class MainController implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String LOG_TAG = MainController.class.getSimpleName();
    public static final int LOCATION_PERMISSIONS = 1;

    private MainActivity mActivity;
    private GoogleApiClient mGoogleApiClient;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private Marker mCurrentLocationMarker;
    private ArrayList<Journey> mJourneys;
    private Journey mCurrentJourney;
    private boolean mIsLocationUpdateStarted;
    private boolean mIsPathRecording;
    private Polyline mLine;
    private PolylineOptions mLineOptions;

    public MainController(MainActivity activity, SupportMapFragment mapFragment){
        mActivity = activity;
        mMapFragment = mapFragment;
        mIsPathRecording = false;
        mIsLocationUpdateStarted = false;
        mJourneys = new ArrayList<>();
        mLineOptions = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        createGoogleApiInstance();
        setupLocationRequest();
        mLocationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        mMapFragment.getMapAsync(this);
    }

    public void createGoogleApiInstance() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void setupLocationRequest(){
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(Constants.LOCATION_REQUEST_ACCURACY);
        mLocationRequest.setInterval(Constants.LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.LOCATION_REQUEST_FASTEST_INTERVAL);
    }

    public void checkPermissions(){
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    mActivity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSIONS
            );

            return;
        }
        getLastLocation();
    }

    public void checkLocationSettings(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. Initialize location requests here.
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. Show a dialog
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(mActivity, Constants.REQUEST_CHECK_SETTINGS);
                        }
                        catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    public void managePermissionResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode) {
            case LOCATION_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    // permission denied, do something (ask for permission again?)
                    //this throws a stackoverflow error due to recursion (show dialog?)
                    //checkPermissions();
                }
                return;
            }
            //other permissions?
        }
    }

    public void startPathRecording(){
        if(!mIsPathRecording){
            mIsPathRecording = true;
            mCurrentJourney = new Journey(System.currentTimeMillis());
        }
    }

    public void stopPathRecording(){
        if(mIsPathRecording){
            mIsPathRecording = false;
            mCurrentJourney.setEndTimestamp(System.currentTimeMillis());
            mJourneys.add(0, mCurrentJourney);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation(){
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mCurrentLocation != null) {
            moveMapCameraToCurrentLocation();
        }
        else{
            Log.d(LOG_TAG, "Can't retrieve last location");
        }
    }

    public ArrayList<Journey> getJourneys(){
        return mJourneys;
    }

    public void startLocationUpdates() {
        //TODO: check location settings?
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    mActivity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSIONS
            );
        }
        else{
            if(!mIsLocationUpdateStarted){
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);
                setLocationUpdateStarted(true);
            }
        }
    }

    public void setLocationUpdateStarted(boolean started){
        mIsLocationUpdateStarted = started;
    }

    /**
     * Moves map over users's current location
     */
    public void moveMapCameraToCurrentLocation(){
        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        CameraUpdate myCamera = CameraUpdateFactory.newLatLng(latLng);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(17);
        mMap.moveCamera(myCamera);
        mMap.animateCamera(zoom);
    }

    public void connectGoogleApiClient(){
        mGoogleApiClient.connect();
    }

    public void disconnectGoogleApiClient(){
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "onMapReady");
        mMap = googleMap;
        if(mCurrentLocation != null){
            moveMapCameraToCurrentLocation();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(LOG_TAG, "OnConnected");
        checkPermissions();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        if (mCurrentLocationMarker != null) {
            mCurrentLocationMarker.remove();
        }
        drawLine(latLng);
        if(mIsPathRecording){
            mCurrentJourney.addPoint(latLng);
        }
        moveMapCameraToCurrentLocation();
    }

    /**
     * Draws line up to current location
     * @param latLng current user location
     */
    private void drawLine(LatLng latLng){
        mLineOptions.add(latLng);
        addMarker(latLng);
        mLine = mMap.addPolyline(mLineOptions);
    }


    /**
     * Adds a marker on map representing the user current location
     * @param latLng current user location
     */
    private void addMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mCurrentLocationMarker = mMap.addMarker(markerOptions);
    }
}
