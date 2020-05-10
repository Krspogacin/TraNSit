package org.mad.transit.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import org.mad.transit.R;
import org.mad.transit.adapters.NavigationAdapter;
import org.mad.transit.fragments.NavigationMapFragment;
import org.mad.transit.model.NavigationStop;
import org.mad.transit.model.Route;
import org.mad.transit.model.RoutePart;
import org.mad.transit.model.Stop;
import org.mad.transit.model.TravelType;

import java.util.ArrayList;
import java.util.List;

public class NavigationActivity extends AppCompatActivity {

    public static final String ROUTE = "route";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_navigation);

        Route route = (Route) this.getIntent().getSerializableExtra(ROUTE);

        ListView listView = this.findViewById(R.id.navigation_bottom_sheet_list);
        List<NavigationStop> stops = new ArrayList<>();
        for (RoutePart routePart : route.getParts()) {
            if (routePart.getTravelType() == TravelType.BUS) {
                for (Stop stop : routePart.getStops()) {
                    if (stops.size() < 1) {
                        stops.add(new NavigationStop(stop, true, -1));
                    } else {
                        stops.add(new NavigationStop(stop, false, stops.size() * 2));
                    }
                }
            }
        }
        NavigationAdapter navigationAdapter = new NavigationAdapter(this, stops);
        listView.setAdapter(navigationAdapter);

        if (navigationAdapter.getCount() > 3) {
            View item = navigationAdapter.getView(0, null, listView);
            item.measure(0, 0);
            ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3 * item.getMeasuredHeight());
            listView.setLayoutParams(params);
        }

        NavigationMapFragment navigationMapFragment = NavigationMapFragment.newInstance();
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.navigation_map_container, navigationMapFragment).commit();
    }
}