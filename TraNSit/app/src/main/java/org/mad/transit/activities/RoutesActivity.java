package org.mad.transit.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.mad.transit.R;
import org.mad.transit.adapters.RoutesAdapter;
import org.mad.transit.fragments.RoutesMapFragment;
import org.mad.transit.model.Route;
import org.mad.transit.model.RoutePart;
import org.mad.transit.model.Stop;
import org.mad.transit.view.model.RoutesViewModel;

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
import static org.mad.transit.model.TravelType.BUS;

public class RoutesActivity extends AppCompatActivity implements RoutesAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private RoutesMapFragment mapFragment;
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
        mapFragment = RoutesMapFragment.newInstance();
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

        ImageView filterIcon = findViewById(R.id.filter_icon);
        filterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RoutesActivity.this, "Filter", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Route route = routesViewModel.getRoutesLiveData().getValue().get(position);
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        mapFragment.clearStopMarkers();
        Stop firstStop = null;
        for (RoutePart part : route.getParts()) {
            if (BUS == part.getTravelType()) {
                for (Stop stop : part.getStops()) {
                    if (firstStop == null) {
                        firstStop = stop;
                    }
                    polylineOptions.add(new LatLng(stop.getLocation().getLatitude(), stop.getLocation().getLongitude()));
                    mapFragment.addStopMarker(stop);
                }
            }
        }
        if (firstStop != null) {
            mapFragment.zoomOnLocation(firstStop.getLocation().getLatitude(), firstStop.getLocation().getLongitude());
            //...
        }
        mapFragment.addPolyline(polylineOptions);
        mapFragment.setSelectedRoute(route);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
