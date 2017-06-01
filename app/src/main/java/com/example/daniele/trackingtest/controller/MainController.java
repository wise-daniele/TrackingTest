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
import com.example.daniele.trackingtest.Utils;
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
    private GoogleMap mJourneyMap;
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
    private SupportMapFragment mMapDetailFragment;
    private Switch mSwitch;

    /**
     * Monitors the connection with the service
     */
    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "OnServiceDisconnected");
            mServiceBounded = false;
        }

        /**
         * Initialize the objects needed to get location updates on the service
         * @param name
         * @param service
         */
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "OnServiceConnected");
            mServiceBounded = true;
            LocationService.LocalBinder mLocalBinder = (LocationService.LocalBinder)service;
            mLocalBinder.registerListener(MainController.this);
            mLocalBinder.setupLocationRequest();
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
        mLineOptions = new PolylineOptions().width(6).color(Color.BLUE).geodesic(true);
        setupLocationRequest();
        mMapFragment = SupportMapFragment.newInstance();
        mMapFragment.getMapAsync(this);
    }

    /**
     * Gets current fragment added in the main fragment container
     * @return
     */
    public Fragment getCurrentMainFragment(){
        return mActivity.getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
    }

    /**
     * @param isJourneyDetail if true the map must show the selected journey detail
     * @return
     */
    public boolean showMapFragment(boolean isJourneyDetail){
        if(getCurrentMainFragment() instanceof SupportMapFragment){
            return false;
        }

        if(!isJourneyDetail){
            if(mMapFragment == null){
                mMapFragment = SupportMapFragment.newInstance();
            }
            replaceFragment(
                    R.id.main_fragment_container,
                    mMapFragment,
                    Constants.MAP_FRAGMENT_TAG,
                    false
            );
        }
        else{
            mMapDetailFragment = SupportMapFragment.newInstance();
            replaceFragment(
                    R.id.main_fragment_container,
                    mMapDetailFragment,
                    Constants.MAP_JOURNEY_FRAGMENT_TAG,
                    true
            );
        }
        return true;
    }

    public void showJourneyOnDetailMap(final Journey journey){
        mMapDetailFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mJourneyMap = googleMap;
                drawJourneyDetailOnMap(journey);
            }
        });
    }

    private void drawJourneyDetailOnMap(Journey journey){
        mJourneyMap.clear();
        moveJourneyMapCameraToLocation(journey.getPath().get(0));
        String textMarkerStart = mActivity.getString(R.string.text_start) + " " +
                Utils.getDateFromTimestamp(journey.getStartTimestamp());
        String textMarkerEnd = mActivity.getString(R.string.text_end) + " " +
                Utils.getDateFromTimestamp(journey.getEndTimestamp());
        ArrayList<LatLng> path = journey.getPath();
        addMarker(
                path.get(path.size()-1),
                textMarkerStart,
                true
        );
        addMarker(
                path.get(0),
                textMarkerEnd,
                true
        );
        drawLine(journey.getPath());
    }

    /**
     * Adds Journeys' List Fragment on main fragment container
     * @return
     */
    public boolean showJourneysFragment(){
        if(getCurrentMainFragment() instanceof JourneysFragment){
            return false;
        }
        JourneysFragment fragment = JourneysFragment.newInstance();
        fragment.setJourneys(mJourneys);
        replaceFragment(
                R.id.main_fragment_container,
                fragment,
                Constants.JOURNEYS_FRAGMENT_TAG,
                true
        );
        mSwitch.setVisibility(View.GONE);
        return true;
    }

    /**
     * Replaces the fragment within a container
     * @param containerId Id of the container of the fragment that will be replaced
     * @param fragment Fragment to add in the container
     * @param tag tag relative to the fragment
     * @param addToBackStack true if the we want to maintain the current fragment in the stack
     * @return
     */
    private int replaceFragment(int containerId, Fragment fragment, String tag, boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, fragment, tag);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        return fragmentTransaction.commit();
    }

    /**
     * Creates GoogleApiClient with the Location Service API
     */
    public void createGoogleApiInstance() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    /**
     * Sets the parameters for the location request
     */
    private void setupLocationRequest(){
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(Constants.LOCATION_REQUEST_ACCURACY);
        mLocationRequest.setInterval(Constants.LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.LOCATION_REQUEST_FASTEST_INTERVAL);
    }

    /**
     * Check the status of location settings. If they are satisfied the method that starts
     * location update service is called; if they're not active it prompts the user to activate them
     */
    public void checkLocationSettings(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        //Check whether the current location settings are satisfied
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

    /**
     * Follows the callback on permission results on activity
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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

    /**
     * Starts journey recording
     */
    private void startPathRecording(){
        if(!mIsPathRecording){
            mIsPathRecording = true;
            if(mCurrentLocation != null){
                LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                mCurrentJourney = new Journey(System.currentTimeMillis(), latLng);
            }
            else{
                mCurrentJourney = new Journey(System.currentTimeMillis());
            }
        }
    }

    /**
     * Stops journey recording and save the journey in the list
     */
    private void stopPathRecording(){
        if(mIsPathRecording){
            mIsPathRecording = false;
            mCurrentJourney.setEndTimestamp(System.currentTimeMillis());
            //Save jurneys with at least 2 updates
            if(mCurrentJourney.getPath().size()>1){
                mJourneys.add(0, mCurrentJourney);
            }
        }
    }

    /**
     * Clear path drawn on map
     * @return
     */
    public boolean clearPath(){
        mMap.clear();
        mLineOptions = new PolylineOptions().width(6).color(Color.BLUE).geodesic(true);
        addMarker(
                new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                mActivity.getString(R.string.text_marker_current_position),
                false
        );
        return true;
    }

    /**
     * Starts location update service. Before starting the service it checks whether location
     * permissions are granted. If not, it prompts the user to enable them
     */
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

    /**
     * Called when the activity goes in the background and tracking is off.
     * This stops location update service and allows to reduce battery consumption
     */
    public void stopLocationUpdates(){
        Log.d(LOG_TAG, "Stop Location Updates");
        if(mServiceBounded){
            mActivity.unbindService(mConnection);
            mActivity.stopService(mLocationServiceIntent);
        }
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

    /**
     * Moves journey ap over location
     */
    private void moveJourneyMapCameraToLocation(LatLng latLng){
        CameraUpdate myCamera = CameraUpdateFactory.newLatLng(latLng);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(17);
        mJourneyMap.moveCamera(myCamera);
        mJourneyMap.animateCamera(zoom);
    }

    /**
     * Connects GoogleApiClient
     */
    private void connectGoogleApiClient(){
        if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
    }

    /**
     * Disconnects GoogleApiClient
     */
    private void disconnectGoogleApiClient(){
        mGoogleApiClient.disconnect();
    }

    /**
     * Callback that ensures that map is ready
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "onMapReady");
        mMap = googleMap;
        if(mCurrentLocation != null){
            moveMapCameraToCurrentLocation();
        }
    }

    /**
     * Called by onBackPressed on Activity
     */
    public void onBackPressed(){
        if(getCurrentMainFragment().getTag().equals(Constants.MAP_FRAGMENT_TAG)){
            mSwitch.setVisibility(View.VISIBLE);
        }
        else{
            mSwitch.setVisibility(View.GONE);
        }
        if(getCurrentMainFragment().getTag().equals(Constants.MAP_JOURNEY_FRAGMENT_TAG)){
            mJourneyMap.clear();
        }
    }

    /**
     * Called when the activity is resumed. Calls checkLocationSettings because it is possible
     * that the user turns off location settings while the app is in the background
     */
    public void onResume(){
        checkLocationSettings();
    }

    /**
     * Called when the activity is started
     */
    public void onStart(){
        connectGoogleApiClient();
    }

    /**
     * Called when the activity is stopped
     */
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
        startLocationUpdates();
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
    private void drawLinePoint(LatLng latLng){
        mLineOptions.add(latLng);
        mMap.addPolyline(mLineOptions);
    }

    private void drawLine(ArrayList<LatLng> journey){
        PolylineOptions polylineOptions = new PolylineOptions().width(6).color(Color.BLUE).geodesic(true);
        for(int i = journey.size()-1; i>=0; i--){
            LatLng latLng = journey.get(i);
            polylineOptions.add(latLng);
            mJourneyMap.addPolyline(polylineOptions);
        }
    }

    /**
     * Adds a marker on map representing the user current location
     * @param latLng coordinates of marker
     * @param title marker title
     * @param isDetailMap true if the map shows the details of a journey, false if the map
     *                    shows user's tarcking
     */
    private void addMarker(LatLng latLng, String title, boolean isDetailMap){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(title);
        if(!isDetailMap){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            mCurrentLocationMarker = mMap.addMarker(markerOptions);
        }
        else{
            if(title.contains(mActivity.getString(R.string.text_start))){
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }
            else{
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
            mJourneyMap.addMarker(markerOptions);
        }

    }

    /**
     * Called periodically by LocationService as soon as a new location is available
     * @param location user current location
     */
    @Override
    public void onLocationUpdate(Location location) {
        Log.d(LOG_TAG, "onLocationUpdate");
        mCurrentLocation = location;
        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        if (mCurrentLocationMarker != null) {
            mCurrentLocationMarker.remove();
        }
        drawLinePoint(latLng);
        addMarker(
                latLng,
                mActivity.getString(R.string.text_marker_current_position),
                false
        );
        if(mIsPathRecording){
            mCurrentJourney.addPoint(latLng);
        }
        moveMapCameraToCurrentLocation();
    }

    /**
     * Listener on switch buttons
     * @param buttonView
     * @param isChecked
     */
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
