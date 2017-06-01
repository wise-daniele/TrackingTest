package com.example.daniele.trackingtest.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.daniele.trackingtest.model.Journey;
import com.example.daniele.trackingtest.R;
import com.example.daniele.trackingtest.ui.adapter.JourneysAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniele on 31/05/17.
 */

public class JourneysFragment extends Fragment{

    public static final String LOG_TAG = JourneysFragment.class.getSimpleName();

    public static JourneysFragment newInstance() {
        JourneysFragment fragment = new JourneysFragment();
        return fragment;
    }

    private RecyclerView mJourneyView;
    private List<Journey> mJourneyList;
    private JourneysAdapter mJourneysAdapter;
    private LinearLayoutManager mLayoutManager;
    private JourneysFragmentListener mListener;
    private JourneysAdapter.OnItemClickListener mItemListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_journeys, container, false);
        mJourneyView = (RecyclerView) rootView.findViewById(R.id.journeys_list);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mJourneyView.setLayoutManager(mLayoutManager);
        mItemListener = new JourneysAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Journey item) {
                mListener.onItemSelected(item);
            }
        };
        mJourneysAdapter = new JourneysAdapter(getActivity(), mJourneyList, mItemListener);
        mJourneyView.setAdapter(mJourneysAdapter);
        return rootView;
    }

    public void setJourneys(ArrayList<Journey> list){
        mJourneyList = list;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (JourneysFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface JourneysFragmentListener {

        void onItemSelected(Journey journey);

    }

}
