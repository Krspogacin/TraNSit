package org.mad.transit.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.activities.SingleLineActivity;
import org.mad.transit.fragments.TimetableFragment;
import org.mad.transit.model.DepartureTime;
import org.mad.transit.view.model.TimetableViewModel;

import java.util.List;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

public class TimetableListAdapter extends BaseAdapter {
    private Activity activity;
    private String day;

    public TimetableListAdapter(Activity activity, String day) {
        this.activity = activity;
        this.day = day;
    }

    @Override
    public int getCount() {
        if (TimetableViewModel.timetableMap.containsKey(day)){
            return TimetableViewModel.timetableMap.get(day).getDepartureTimes().size();
        }else{
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (TimetableViewModel.timetableMap.containsKey(day)){
            return TimetableViewModel.timetableMap.get(day).getDepartureTimes().get(position);
        }else{
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
        DepartureTime time = TimetableViewModel.timetableMap.get(day).getDepartureTimes().get(position);

        if (convertView == null) {
            view = activity.getLayoutInflater().inflate(R.layout.timetable_list_item, null);
        }

        TextView name = view.findViewById(R.id.time);
        name.setText(time.getFormattedValue());

        return view;
    }
}
