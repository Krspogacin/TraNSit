package org.mad.transit.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.model.Line;

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

        ImageView icon = view.findViewById(R.id.line_icon);
        icon.setImageResource(R.drawable.ic_line_number_blue_icon);

        TextView number = view.findViewById(R.id.line_number);
        number.setText(line.getNumber());

        TextView name = view.findViewById(R.id.line_name);
        name.setText(line.getTitle());

        return view;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }
}