package com.example.daniele.trackingtest.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.daniele.trackingtest.Journey;

import java.util.List;

/**
 * Created by daniele on 31/05/17.
 */

public class JourneysAdapter extends RecyclerView.Adapter<JourneysAdapter.JourneyViewHolder>  {

    protected class JourneyViewHolder extends RecyclerView.ViewHolder {

        View myView;

        public JourneyViewHolder(View view) {
            super(view);
            myView = view;
        }
    }

    private Context mContext;
    private List<Journey> mList;

    public JourneysAdapter(Context context, List<Journey> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public JourneyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(JourneyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
