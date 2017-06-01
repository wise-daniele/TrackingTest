package com.example.daniele.trackingtest.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.daniele.trackingtest.R;

/**
 * Created by daniele on 01/06/17.
 */

public class JourneyDetailFragment extends Fragment {

    public static final String LOG_TAG = JourneyDetailFragment.class.getSimpleName();

    public static JourneyDetailFragment newInstance() {
        JourneyDetailFragment fragment = new JourneyDetailFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_journey_detail, container, false);
        return rootView;
    }

}
