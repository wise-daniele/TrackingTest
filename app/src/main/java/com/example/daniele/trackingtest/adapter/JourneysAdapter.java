package com.example.daniele.trackingtest.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.daniele.trackingtest.Journey;
import com.example.daniele.trackingtest.R;
import com.example.daniele.trackingtest.Utils;

import java.util.List;

/**
 * Created by daniele on 31/05/17.
 */

public class JourneysAdapter extends RecyclerView.Adapter<JourneysAdapter.JourneyViewHolder>  {

    public static class JourneyViewHolder extends RecyclerView.ViewHolder {

        View myView;
        TextView title;
        TextView startDate;
        TextView endDate;

        public JourneyViewHolder(View view) {
            super(view);
            myView = view;
            title = (TextView) view.findViewById(R.id.journey_title);
            startDate = (TextView) view.findViewById(R.id.journey_start_date);
            endDate = (TextView) view.findViewById(R.id.journey_end_date);
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
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.view_item_journey, parent, false);
        return new JourneyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(JourneyViewHolder holder, int position) {
        final Journey journey = mList.get(position);
        if(journey != null) {
            holder.title.setText(mContext.getString(R.string.text_journey_title, position));
            String textStart = Utils.getDateFromTimestamp(journey.getStartTimestamp());
            String textEnd = Utils.getDateFromTimestamp(journey.getEndTimestamp());
            holder.startDate.setText(textStart);
            holder.endDate.setText(textEnd);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
