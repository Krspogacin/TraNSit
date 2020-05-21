package org.mad.transit.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.model.DepartureTime;
import org.mad.transit.model.TimetableViewModel;

public class TimetableListAdapter extends BaseAdapter {
    private Activity activity;

    public TimetableListAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return TimetableViewModel.getTimeTables().size();
    }

    @Override
    public Object getItem(int position) {
        return TimetableViewModel.getTimeTables().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        DepartureTime time = TimetableViewModel.getTimeTables().get(position);

        if (convertView == null) {
            view = activity.getLayoutInflater().inflate(R.layout.timetable_list_item, null);
        }

        TextView name = view.findViewById(R.id.time);
        name.setText(time.getFormattedValue());

        return view;
    }
}
