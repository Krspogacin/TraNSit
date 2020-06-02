package org.mad.transit.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.model.Line;
import org.mad.transit.model.LineType;
import org.mad.transit.view.model.LinesFragmentViewModel;

public class LinesAdapter extends BaseAdapter {
    private final Activity activity;

    public LinesAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return LinesFragmentViewModel.getLines().size();
    }

    @Override
    public Object getItem(int position) {
        return LinesFragmentViewModel.getLines().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Line line = LinesFragmentViewModel.getLines().get(position);

        if (convertView == null) {
            view = this.activity.getLayoutInflater().inflate(R.layout.lines_list_item, null);
        }

        TextView name = view.findViewById(R.id.line_name);
        name.setText(line.getTitle());

        TextView number = view.findViewById(R.id.line_number);
        number.setText(line.getNumber());

        TextView type = view.findViewById(R.id.line_type);
        ImageView image = view.findViewById(R.id.line_icon);
        if (line.getType() == LineType.CITY) {
            image.setImageResource(R.drawable.ic_line_number_accent_icon);
            type.setText(R.string.city);
        } else if (line.getType() == LineType.SUBURBAN) {
            image.setImageResource(R.drawable.ic_line_number_primary_icon);
            type.setText(R.string.suburban);
        } else if (line.getType() == LineType.INTERCITY) {
            image.setImageResource(R.drawable.ic_line_number_primary_dark_icon);
            type.setText(R.string.intercity);
        }
        return view;
    }
}
