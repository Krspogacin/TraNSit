package org.mad.transit.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.mad.transit.R;
import org.mad.transit.model.Line;
import org.mad.transit.model.NearbyStop;

import java.util.ArrayList;

public class StopsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity context;
    private final ArrayList<NearbyStop> nearbyStops;
    private final OnItemClickListener onItemClickListener;

    public StopsAdapter(Activity context, ArrayList<NearbyStop> nearbyStops, OnItemClickListener onItemClickListener) {
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

        viewHolder.stopTitle.setText(nearbyStop.getName());
        viewHolder.stopWalkTime.setText(this.context.getString(R.string.walk_time, nearbyStop.getWalkTime()));

        for (Line line : nearbyStop.getLines()) {
            View lineNumberView = this.context.getLayoutInflater().inflate(R.layout.line_number, null);
            TextView lineNumberTextView = lineNumberView.findViewById(R.id.stop_line_small_number);
            lineNumberTextView.setText(line.getNumber());
            viewHolder.linesContainer.addView(lineNumberView);
        }
    }

    @Override
    public int getItemCount() {
        return this.nearbyStops.size();
    }

    private static class RecyclerViewViewHolder extends RecyclerView.ViewHolder {
        private final TextView stopTitle;
        private final TextView stopWalkTime;
        private final LinearLayout linesContainer;

        private RecyclerViewViewHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);
            this.stopTitle = itemView.findViewById(R.id.stop_title);
            this.stopWalkTime = itemView.findViewById(R.id.stop_walk_time);
            this.linesContainer = itemView.findViewById(R.id.lines_container);

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