package org.mad.transit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.model.NavigationItem;

import java.util.ArrayList;

public class NavigationDrawerListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<NavigationItem> navigationItems;

    public NavigationDrawerListAdapter(Context context, ArrayList<NavigationItem> navigationItems) {
        this.context = context;
        this.navigationItems = navigationItems;
    }

    @Override
    public int getCount() {
        return navigationItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navigationItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.navigation_drawer_list_item, null);
        }

        TextView name = view.findViewById(R.id.drawer_item_name);
        name.setText(navigationItems.get(position).getTitle());

        ImageView image = view.findViewById(R.id.drawer_item_icon);
        image.setImageResource(navigationItems.get(position).getIcon());
        return view;
    }
}
