package com.example.daniele.trackingtest.controller;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.daniele.trackingtest.Constants;
import com.example.daniele.trackingtest.R;
import com.example.daniele.trackingtest.model.Journey;
import com.example.daniele.trackingtest.service.LocationService;
import com.example.daniele.trackingtest.ui.JourneysFragment;
import com.example.daniele.trackingtest.ui.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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
        GoogleApiClient.OnConnectionFailedListener, LocationService.LocationUpdateListener,
        CompoundButton.OnCheckedChangeListener{

    public static final String LOG_TAG = MainController.class.getSimpleName();

    private MainActivity mActivity;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Intent mLocationServiceIntent;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private Marker mCurrentLocationMarker;
    private ArrayList<Journey> mJourneys;
    private Journey mCurrentJourney;
    private boolean mIsLocationUpdateStarted;
    private boolean mIsPathRecording;
    private Polyline mLine;
    private PolylineOptions mLineOptions;
    private boolean mServiceBounded;

    private SupportMapFragment mMapFragment;
    private Switch mSwitch;

    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "OnServiceDisconnected");
            mServiceBounded = false;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "OnSErviceConnected");
            mServiceBounded = true;
            LocationService.LocalBinder mLocalBinder = (LocationService.LocalBinder)service;
            mLocalBinder.registerListener(MainController.this);
            mLocalBinder.setGoogleApiClient(mGoogleApiClient);
        }
    };

    public MainController(MainActivity activity, Switch switchButton){
        mActivity = activity;
        mSwitch = switchButton;
        mSwitch.setOnCheckedChangeListener(this);
        mIsPathRecording = false;
        mIsLocationUpdateStarted = false;
        createGoogleApiInstance();
        mJourneys = new ArrayList<>();
        mLineOptions = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        setupLocationRequest();
        mMapFragment = SupportMapFragment.newInstance();
        mMapFragment.getMapAsync(this);
    }

    public Fragment getCurrentMainFragment(){
        return mActivity.getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
    }

    public boolean showMapFragment(){
        if(getCurrentMainFragment() instanceof SupportMapFragment){
            return false;
        }
        if(mMapFragment == null){
            mMapFragment =  SupportMapFragment.newInstance();
        }
        replaceFragment(
                R.id.main_fragment_container,
                mMapFragment,
                Constants.MAP_FRAGMENT_TAG, false
        );
        return true;
    }

    public boolean showJourneysFragment(){
        if(getCurrentMainFragment() instanceof JourneysFragment){
            return false;
        }
        JourneysFragment fragment = JourneysFragment.newInstance();
        fragment.setJourneys(getJourneys());
        replaceFragment(
                R.id.main_fragment_container,
                fragment,
                Constants.JOURNEYS_FRAGMENT_TAG,
                true
        );
        mSwitch.setVisibility(View.GONE);
        return true;
    }

    private int replaceFragment(int containerId, Fragment fragment, String tag, boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, fragment, tag);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        return fragmentTransaction.commit();
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

    private void setupLocationRequest(){
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(Constants.LOCATION_REQUEST_ACCURACY);
        mLocationRequest.setInterval(Constants.LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.LOCATION_REQUEST_FASTEST_INTERVAL);
    }

    private void checkPermissions(){
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    mActivity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.LOCATION_PERMISSIONS
            );

            return;
        }
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
            case Constants.LOCATION_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    // permission denied, do something (ask for permission again?)
                    // No, it throws a stackoverflow error due to recursion
                }
                return;
            }
            //other permissions?
        }
    }

    private void startPathRecording(){
        if(!mIsPathRecording){
            mIsPathRecording = true;
            mCurrentJourney = new Journey(System.currentTimeMillis());
        }
    }

    private void stopPathRecording(){
        if(mIsPathRecording){
            mIsPathRecording = false;
            mCurrentJourney.setEndTimestamp(System.currentTimeMillis());
            mJourneys.add(0, mCurrentJourney);
        }
    }

    public boolean clearPath(){
        mMap.clear();
        addMarker(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        return true;
    }

    public ArrayList<Journey> getJourneys(){
        return mJourneys;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    mActivity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.LOCATION_PERMISSIONS
            );
        }
        else{
            if(!mIsLocationUpdateStarted){
                Log.d(LOG_TAG, "Start Service");
                mLocationServiceIntent = new Intent(mActivity, LocationService.class);
                mActivity.bindService(mLocationServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
                mIsLocationUpdateStarted = true;
            }
        }
    }

    public void stopLocationUpdates(){
        Log.d(LOG_TAG, "Stop Location Updates");
        mActivity.unbindService(mConnection);
        mActivity.stopService(mLocationServiceIntent);
    }

    private void setLocationUpdateStarted(boolean started){
        mIsLocationUpdateStarted = started;
    }

    /**
     * Moves map over users's current location
     */
    private void moveMapCameraToCurrentLocation(){
        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        CameraUpdate myCamera = CameraUpdateFactory.newLatLng(latLng);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(17);
        mMap.moveCamera(myCamera);
        mMap.animateCamera(zoom);
    }

    private void connectGoogleApiClient(){
        mGoogleApiClient.connect();
    }

    private void disconnectGoogleApiClient(){
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

    public void onBackPressed(){
        if(getCurrentMainFragment().getTag().equals(Constants.MAP_FRAGMENT_TAG)){
            mSwitch.setVisibility(View.VISIBLE);
        }
        else{
            mSwitch.setVisibility(View.GONE);
        }
    }

    public void onResume(){
        //It is possible that the user turns off location settings while the app is in background
        checkLocationSettings();
    }

    public void onStart(){
        if(!mSwitch.isChecked()){
            connectGoogleApiClient();
        }
    }

    public void onStop(){
        Log.d(LOG_TAG, "OnStop");
        if(!mSwitch.isChecked()){
            stopLocationUpdates();
            setLocationUpdateStarted(false);
            disconnectGoogleApiClient();
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

    @Override
    public void onLocationUpdate(Location location) {
        Log.d(LOG_TAG, "onLocationUpdate");
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId() == R.id.switch_button){
            if(isChecked){
                startPathRecording();
            }
            else{
                stopPathRecording();
            }
        }
    }
}
