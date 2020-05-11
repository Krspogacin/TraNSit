package org.mad.transit.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.model.Line;
import org.mad.transit.model.Stop;

public class StopLinesAdapter extends BaseAdapter {
    private final Activity activity;
    private final Stop stop;

    public StopLinesAdapter(Activity activity, Stop stop) {
        this.activity = activity;
        this.stop = stop;
    }

    @Override
    public int getCount() {
        return this.stop.getLines().size();
    }

    @Override
    public Object getItem(int position) {
        return this.stop.getLines().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Line line = this.stop.getLines().get(position);

        if (convertView == null) {
            view = this.activity.getLayoutInflater().inflate(R.layout.stop_lines_list_item, null);
        }

        TextView number = view.findViewById(R.id.stop_line_number);
        number.setText(line.getNumber());

        TextView name = view.findViewById(R.id.stop_line_name);
        name.setText(line.getTitle());

        TextView nextDeparture = view.findViewById(R.id.stop_line_next_departure);
        nextDeparture.setText(line.getNextDeparture());

        return view;
    }
}