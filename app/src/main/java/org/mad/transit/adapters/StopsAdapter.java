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

import java.util.ArrayList;

public class StopsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity context;
    private final ArrayList<NearbyStop> nearbyStops;

    public StopsAdapter(Activity context, ArrayList<NearbyStop> nearbyStops) {
        this.context = context;
        this.nearbyStops = nearbyStops;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(this.context).inflate(R.layout.stops_bottom_sheet_list_item, parent, false);
        return new RecyclerViewViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        NearbyStop nearbyStop = this.nearbyStops.get(position);
        RecyclerViewViewHolder viewHolder = (RecyclerViewViewHolder) holder;

        viewHolder.stopTitle.setText(nearbyStop.getName());
        viewHolder.stopWalkTime.setText(this.context.getString(R.string.walk_time, nearbyStop.getWalkTime()));
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < nearbyStop.getLines().length - 1; i++) {
            lines.append(nearbyStop.getLines()[i]).append(",");
        }
        lines.append(nearbyStop.getLines()[nearbyStop.getLines().length - 1]);
        viewHolder.stopLines.setText(this.context.getString(R.string.lines, lines.toString()));
    }

    @Override
    public int getItemCount() {
        return this.nearbyStops.size();
    }

    private static class RecyclerViewViewHolder extends RecyclerView.ViewHolder {
        private final TextView stopTitle;
        private final TextView stopWalkTime;
        private final TextView stopLines;

        private RecyclerViewViewHolder(@NonNull View itemView) {
            super(itemView);
            this.stopTitle = itemView.findViewById(R.id.stop_title);
            this.stopWalkTime = itemView.findViewById(R.id.stop_walk_time);
            this.stopLines = itemView.findViewById(R.id.stop_lines);
        }
    }
}