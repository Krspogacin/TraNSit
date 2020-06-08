package org.mad.transit.adapters;

import android.app.Activity;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.mad.transit.R;

import java.util.List;

public class TimetableListAdapter extends BaseAdapter {
    private final Activity activity;
    private final List<String> groups;
    private final List<Spanned> departureTimes;

    public TimetableListAdapter(Activity activity, List<String> groups, List<Spanned> departureTimes) {
        this.activity = activity;
        this.groups = groups;
        this.departureTimes = departureTimes;
    }

    @Override
    public int getCount() {
        return this.groups.size();
    }

    @Override
    public Object getItem(int position) {
        return this.groups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        String group = this.groups.get(position);
        Spanned departureTime = this.departureTimes.get(position);

        if (convertView == null) {
            view = this.activity.getLayoutInflater().inflate(R.layout.timetable_list_item, null);
        }

        TextView timeGroup = view.findViewById(R.id.time_group);
        timeGroup.setText(group);

        TextView timeMinutes = view.findViewById(R.id.time_minutes);
        timeMinutes.setText(departureTime);

        return view;
    }
}
