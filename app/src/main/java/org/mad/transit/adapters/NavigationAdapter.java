package org.mad.transit.adapters;

import android.app.Activity;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.model.NavigationStop;

import java.util.List;

public class NavigationAdapter extends BaseAdapter {
    private final Activity activity;
    private final List<NavigationStop> stops;

    public NavigationAdapter(Activity activity, List<NavigationStop> stops) {
        this.activity = activity;
        this.stops = stops;
    }

    @Override
    public int getCount() {
        return this.stops.size();
    }

    @Override
    public Object getItem(int position) {
        return this.stops.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        NavigationStop navigationStop = this.stops.get(position);

        if (convertView == null) {
            view = this.activity.getLayoutInflater().inflate(R.layout.navigation_bottom_sheet_list_item, null);
        }

        TextView stopTitleTextView = view.findViewById(R.id.navigation_stop_title);
        stopTitleTextView.setText(navigationStop.getTitle());

        TextView stopTimeTextView = view.findViewById(R.id.navigation_stop_time);
        stopTimeTextView.setText(this.activity.getString(R.string.stopTime, String.valueOf(navigationStop.getMinutes())));

        if (navigationStop.isPassed()) {
            this.stopIsPassed((ImageView) view.findViewById(R.id.navigation_bus_icon), stopTitleTextView, stopTimeTextView);
        }

        return view;
    }

    private void stopIsPassed(ImageView navigationBusIcon, TextView stopTitleTextView, TextView stopTimeTextView) {
        navigationBusIcon.setImageResource(R.drawable.bus_dark_icon);
        stopTitleTextView.setPaintFlags(stopTitleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        stopTitleTextView.setTextColor(this.activity.getResources().getColor(R.color.transparentBlack));
        stopTimeTextView.setVisibility(View.INVISIBLE);
    }
}