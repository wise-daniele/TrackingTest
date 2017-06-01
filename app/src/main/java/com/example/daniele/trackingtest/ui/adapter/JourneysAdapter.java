package com.example.daniele.trackingtest.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.daniele.trackingtest.model.Journey;
import com.example.daniele.trackingtest.R;
import com.example.daniele.trackingtest.Utils;

import java.util.List;

/**
 * Created by daniele on 31/05/17.
 */

public class JourneysAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

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
    private OnItemClickListener mListener;

    public JourneysAdapter(Context context, List<Journey> list, OnItemClickListener listener) {
        mContext = context;
        mList = list;
        mListener = listener;
    }

    @Override
    public JourneyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.view_item_journey, parent, false);
        return new JourneyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Journey journey = mList.get(position);
        if(journey != null) {
            ((JourneyViewHolder)holder).title.setText(mContext.getString(R.string.text_journey_title, position));
            String textStart = Utils.getDateFromTimestamp(journey.getStartTimestamp());
            String textEnd = Utils.getDateFromTimestamp(journey.getEndTimestamp());
            ((JourneyViewHolder)holder).startDate.setText(textStart);
            ((JourneyViewHolder)holder).endDate.setText(textEnd);
            ((JourneyViewHolder)holder).myView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemClick(journey);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Journey item);
    }
}
