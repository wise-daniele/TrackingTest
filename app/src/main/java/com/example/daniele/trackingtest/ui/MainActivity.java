package com.example.daniele.trackingtest.ui;

import android.app.Activity;
import android.content.Intent;
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
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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
    protected void onStart() {
        mMainController.checkLocationSettings();
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