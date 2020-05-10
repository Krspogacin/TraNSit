package org.mad.transit.activities;

import android.os.Bundle;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.adapters.RoutesAdapter;
import org.mad.transit.fragments.MapFragment;
import org.mad.transit.fragments.RoutesMapFragment;
import org.mad.transit.model.Route;
import org.mad.transit.model.RoutesViewModel;

import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static org.mad.transit.fragments.DirectionsFragment.END_POINT;
import static org.mad.transit.fragments.DirectionsFragment.START_POINT;

public class RoutesActivity extends AppCompatActivity implements RoutesAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private MapFragment mapFragment;
    private RoutesViewModel routesViewModel;

    private final Observer<List<Route>> routesListUpdateObserver = new Observer<List<Route>>() {
        @Override
        public void onChanged(List<Route> routes) {
            RoutesAdapter routesAdapter = new RoutesAdapter(RoutesActivity.this, routes, RoutesActivity.this);
            recyclerView.setLayoutManager(new LinearLayoutManager(RoutesActivity.this));
            recyclerView.setAdapter(routesAdapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        recyclerView = findViewById(R.id.routes_bottom_sheet_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        routesViewModel = new ViewModelProvider(this).get(RoutesViewModel.class);
        routesViewModel.getRoutesLiveData().observe(RoutesActivity.this, routesListUpdateObserver);
        mapFragment = RoutesMapFragment.newInstance(routesViewModel);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.routes_map_container, mapFragment)
                .commit();

        Toolbar toolbar = findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        String startPointText = getIntent().getStringExtra(START_POINT);
        String endPointText = getIntent().getStringExtra(END_POINT);

        TextView startPoint = findViewById(R.id.start_point_text);
        startPoint.setText(startPointText);
        TextView endPoint = findViewById(R.id.end_point_text);
        endPoint.setText(endPointText);
    }

    @Override
    public void onItemClick(int position) {
//        Route route = routesViewModel.getRoutesLiveData().getValue().get(position);
//        stopsMapFragment.zoomOnLocation(nearbyStop.getLatitude(), nearbyStop.getLongitude());
//        stopsMapFragment.updateFloatingLocationButton(false);
//        stopsMapFragment.getNearbyStopsMarkers().get(position).showInfoWindow();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
