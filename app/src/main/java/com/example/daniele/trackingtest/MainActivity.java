package com.example.daniele.trackingtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends AppCompatActivity{

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final int LOCATION_PERMISSIONS = 1;

    private MainController mMainController;
    private SupportMapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMainController = new MainController(this, mMapFragment);
        mMainController.createGoogleApiInstance();
    }

    @Override
    protected void onStart() {
        mMainController.connectGoogleApiClient();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mMainController.disconnectGoogleApiClient();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mMainController.managePermissionResult(requestCode, permissions, grantResults);
    }
}
