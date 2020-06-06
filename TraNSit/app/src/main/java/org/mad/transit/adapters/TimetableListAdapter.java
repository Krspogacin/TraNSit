package org.mad.transit.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.model.DepartureTime;
import org.mad.transit.view.model.TimetableViewModel;

public class TimetableListAdapter extends BaseAdapter {
    private final Activity activity;
    private final String day;
    private final TimetableViewModel timetableViewModel;

    public TimetableListAdapter(Activity activity, String day, TimetableViewModel timetableViewModel) {
        this.activity = activity;
        this.day = day;
        this.timetableViewModel = timetableViewModel;
    }

    @Override
    public int getCount() {
        if (this.timetableViewModel.getTimetableMap().containsKey(this.day)) {
            return this.timetableViewModel.getTimetableMap().get(this.day).getDepartureTimes().size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (this.timetableViewModel.getTimetableMap().containsKey(this.day)) {
            return this.timetableViewModel.getTimetableMap().get(this.day).getDepartureTimes().get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        DepartureTime time = this.timetableViewModel.getTimetableMap().get(this.day).getDepartureTimes().get(position);

        if (convertView == null) {
            view = this.activity.getLayoutInflater().inflate(R.layout.timetable_list_item, null);
        }

        TextView name = view.findViewById(R.id.time);
        name.setText(time.getFormattedValue());

        return view;
    }
}
