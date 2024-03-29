package org.mad.transit.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.mad.transit.R;
import org.mad.transit.model.NearbyStop;

import java.util.List;

public class StopsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity context;
    private final List<NearbyStop> nearbyStops;
    private final OnItemClickListener onItemClickListener;

    public StopsAdapter(Activity context, List<NearbyStop> nearbyStops, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.nearbyStops = nearbyStops;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(this.context).inflate(R.layout.stops_bottom_sheet_list_item, parent, false);
        return new RecyclerViewViewHolder(rootView, this.onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        NearbyStop nearbyStop = this.nearbyStops.get(position);
        RecyclerViewViewHolder viewHolder = (RecyclerViewViewHolder) holder;

        viewHolder.stopTitle.setText(nearbyStop.getTitle());
        viewHolder.stopWalkTime.setText(this.context.getString(R.string.walk_time, nearbyStop.getWalkTime()));
    }

    @Override
    public int getItemCount() {
        return this.nearbyStops.size();
    }

    private static class RecyclerViewViewHolder extends RecyclerView.ViewHolder {
        private final TextView stopTitle;
        private final TextView stopWalkTime;

        private RecyclerViewViewHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);
            this.stopTitle = itemView.findViewById(R.id.stop_title);
            this.stopWalkTime = itemView.findViewById(R.id.stop_walk_time);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(RecyclerViewViewHolder.this.getAdapterPosition());
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}