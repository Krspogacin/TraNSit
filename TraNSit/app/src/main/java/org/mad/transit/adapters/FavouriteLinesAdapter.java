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

import java.util.List;

public class FavouriteLinesAdapter extends BaseAdapter {
    private final Activity activity;
    private List<Line> lines;

    public FavouriteLinesAdapter(Activity activity, List<Line> lines) {
        this.activity = activity;
        this.lines = lines;
    }

    @Override
    public int getCount() {
        return this.lines.size();
    }

    @Override
    public Object getItem(int position) {
        return this.lines.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Line line = this.lines.get(position);

        if (convertView == null) {
            view = this.activity.getLayoutInflater().inflate(R.layout.lines_list_item, null);
        }

        TextView number = view.findViewById(R.id.line_number);
        number.setText(line.getNumber());

        TextView name = view.findViewById(R.id.line_name);
        name.setText(line.getTitle());

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

    public void setLines(List<Line> lines) {
        this.lines = lines;
        this.notifyDataSetChanged();
    }
}