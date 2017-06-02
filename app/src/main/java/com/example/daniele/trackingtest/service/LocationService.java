package com.example.daniele.trackingtest.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.daniele.trackingtest.Constants;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


/**
 * Created by daniele on 01/06/17.
 */

public class LocationService extends Service implements LocationListener {

    public static final String LOG_TAG = LocationService.class.getSimpleName();

    private IBinder mBinder = new LocalBinder();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationUpdateListener mListener;

    /**
     * Called when the service is started.
     * The service is not called if started through bindService
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public void onDestroy(){
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        mListener.onLocationUpdate(location);
    }

    /**
     * Class used for the client Binder.
     */
    public class LocalBinder extends Binder {

        /**
         * Registener the listener in order to listen to location updates
         * @param listener location updates listener
         */
        public void registerListener(LocationUpdateListener listener) {
            mListener = listener;
        }

        public void setupLocationRequest(){
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(Constants.LOCATION_REQUEST_ACCURACY);
            mLocationRequest.setInterval(Constants.LOCATION_REQUEST_INTERVAL);
            mLocationRequest.setFastestInterval(Constants.LOCATION_REQUEST_FASTEST_INTERVAL);
        }

        @SuppressWarnings("MissingPermission")
        public void setGoogleApiClient(GoogleApiClient client) {
            mGoogleApiClient = client;
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, LocationService.this);
        }
    }

    public interface LocationUpdateListener {
        void onLocationUpdate(Location location);
    }

}
