package com.example.daniele.trackingtest.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;

import com.example.daniele.trackingtest.Constants;
import com.example.daniele.trackingtest.R;
import com.example.daniele.trackingtest.controller.MainController;
import com.example.daniele.trackingtest.model.Journey;

public class MainActivity extends AppCompatActivity implements JourneysFragment.JourneysFragmentListener {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private MainController mMainController;
    private Switch mSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwitch = (Switch) findViewById(R.id.switch_button);
        mMainController = new MainController(this, mSwitch);
        if (findViewById(R.id.main_fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            mMainController.showMapFragment(false);
        }
        mMainController.createGoogleApiInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_list) {
            return mMainController.showJourneysFragment();
        }
        if (id == R.id.action_clear_path) {
            return mMainController.clearPath();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mMainController.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainController.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMainController.onStart();
    }

    @Override
    protected void onStop() {
        mMainController.onStop();
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

    @Override
    public void onItemSelected(Journey journey) {
        mMainController.showMapFragment(true);
        mMainController.showJourneyOnDetailMap(journey);
    }
}
