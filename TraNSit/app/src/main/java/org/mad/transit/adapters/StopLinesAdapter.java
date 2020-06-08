package org.mad.transit.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.activities.SingleLineActivity;
import org.mad.transit.activities.TimetableActivity;
import org.mad.transit.model.Line;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.LineType;
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
        final Line line = this.stop.getLines().get(position);

        if (convertView == null) {
            view = this.activity.getLayoutInflater().inflate(R.layout.stop_lines_list_item, null);
        }

        TextView number = view.findViewById(R.id.stop_line_number);
        number.setText(line.getNumber());

        TextView name = view.findViewById(R.id.stop_line_name);
        name.setText(line.getTitle());

        TextView nextDeparture = view.findViewById(R.id.stop_line_next_departure);
        TextView showTimetable = view.findViewById(R.id.show_timetable);
        if (line.getNextDepartures() == null || line.getNextDepartures().isEmpty()) {
            nextDeparture.setVisibility(View.GONE);
            showTimetable.setVisibility(View.GONE);
        } else {
            nextDeparture.setVisibility(View.VISIBLE);
            showTimetable.setVisibility(View.VISIBLE);
            nextDeparture.setText(this.activity.getString(R.string.next_departure, line.getNextDepartures()));
            showTimetable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(StopLinesAdapter.this.activity, TimetableActivity.class);
                    intent.putExtra(SingleLineActivity.LINE_NAME_KEY, line.getTitle().split("-"));
                    intent.putExtra(SingleLineActivity.LINE_KEY, line);
                    intent.putExtra(SingleLineActivity.DIRECTION_KEY, line.getLineDirectionA() != null ? LineDirection.A : LineDirection.B);
                    StopLinesAdapter.this.activity.startActivity(intent);
                }
            });
        }

        ImageView image = view.findViewById(R.id.stop_line_icon);
        if (line.getType() == LineType.CITY) {
            image.setImageResource(R.drawable.ic_line_number_accent_icon);
        } else if (line.getType() == LineType.SUBURBAN) {
            image.setImageResource(R.drawable.ic_line_number_primary_icon);
        } else if (line.getType() == LineType.INTERCITY) {
            image.setImageResource(R.drawable.ic_line_number_primary_dark_icon);
        }

        return view;
    }
}