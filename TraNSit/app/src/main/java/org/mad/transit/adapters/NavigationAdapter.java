package org.mad.transit.adapters;

import android.app.Activity;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.mad.transit.R;
import org.mad.transit.model.NavigationStop;

import java.util.List;

public class NavigationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity context;
    private final List<NavigationStop> navigationStops;

    public NavigationAdapter(Activity context, List<NavigationStop> navigationStops) {
        this.context = context;
        this.navigationStops = navigationStops;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(this.context).inflate(R.layout.navigation_bottom_sheet_list_item, parent, false);
        return new RecyclerViewViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NavigationStop navigationStop = this.navigationStops.get(position);
        RecyclerViewViewHolder viewHolder = (RecyclerViewViewHolder) holder;

        viewHolder.stopTitleTextView.setText(navigationStop.getTitle());
        viewHolder.stopTimeTextView.setText(navigationStop.getArriveTime());

        if (navigationStop.isPassed()) {
            this.stopIsPassed(viewHolder.busIcon, viewHolder.stopTitleTextView, viewHolder.stopTimeTextView);
        }
    }

    @Override
    public int getItemCount() {
        return this.navigationStops.size();
    }

    private class RecyclerViewViewHolder extends RecyclerView.ViewHolder {
        private final TextView stopTitleTextView;
        private final TextView stopTimeTextView;
        private final ImageView busIcon;

        private RecyclerViewViewHolder(@NonNull View itemView) {
            super(itemView);
            this.stopTitleTextView = itemView.findViewById(R.id.navigation_stop_title);
            this.stopTimeTextView = itemView.findViewById(R.id.navigation_stop_time);
            this.busIcon = itemView.findViewById(R.id.navigation_bus_icon);
        }
    }

    private void stopIsPassed(ImageView navigationBusIcon, TextView stopTitleTextView, TextView stopTimeTextView) {
        navigationBusIcon.setImageResource(R.drawable.bus_dark_icon);
        stopTitleTextView.setPaintFlags(stopTitleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        stopTitleTextView.setTextColor(this.context.getResources().getColor(R.color.transparentBlack));
        stopTimeTextView.setVisibility(View.INVISIBLE);
    }
}
