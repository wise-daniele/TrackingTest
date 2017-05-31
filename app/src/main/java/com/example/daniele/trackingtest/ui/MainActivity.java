package com.example.daniele.trackingtest.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.daniele.trackingtest.Constants;
import com.example.daniele.trackingtest.R;
import com.example.daniele.trackingtest.controller.MainController;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends AppCompatActivity{

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private MainController mMainController;
    private SupportMapFragment mMapFragment;

    private Switch mSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.main_fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            mMapFragment =  SupportMapFragment.newInstance();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.main_fragment_container, mMapFragment);
            fragmentTransaction.commit();
        }

        mMainController = new MainController(this, mMapFragment);
        mMainController.createGoogleApiInstance();

        mSwitch = (Switch) findViewById(R.id.switch_button);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mMainController.startPathRecording();
                }
                else{
                    //TODO:
                    mMainController.stopPathRecording();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainController.setLocationUpdateStarted(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //It is possible that the user turns off location settings while the app is in background
        mMainController.checkLocationSettings();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mMainController.checkLocationSettings();
                        break;
                    case Activity.RESULT_CANCELED:
                        //keep asking for Location Settings
                        mMainController.checkLocationSettings();
                        break;
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mMainController.managePermissionResult(requestCode, permissions, grantResults);
    }
}
