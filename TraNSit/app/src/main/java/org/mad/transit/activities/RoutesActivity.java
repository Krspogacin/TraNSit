package org.mad.transit.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.adapters.RoutesAdapter;
import org.mad.transit.fragments.RoutesMapFragment;
import org.mad.transit.model.Location;
import org.mad.transit.model.Route;
import org.mad.transit.model.RoutePart;
import org.mad.transit.model.Stop;
import org.mad.transit.search.SearchService;
import org.mad.transit.task.RouteSearchAsyncTask;
import org.mad.transit.task.TaskListener;
import org.mad.transit.view.model.RouteViewModel;

import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static org.mad.transit.fragments.DirectionsFragment.END_POINT;
import static org.mad.transit.fragments.DirectionsFragment.START_POINT;
import static org.mad.transit.model.TravelType.BUS;

public class RoutesActivity extends AppCompatActivity implements RoutesAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private RoutesMapFragment mapFragment;
    private FrameLayout loadingOverlay;

    @Inject
    RouteViewModel routeViewModel;

    @Inject
    SearchService searchService;

    private final Observer<List<Route>> routesListUpdateObserver = new Observer<List<Route>>() {
        @Override
        public void onChanged(List<Route> routes) {
            RoutesAdapter routesAdapter = new RoutesAdapter(RoutesActivity.this, routes, RoutesActivity.this);
            RoutesActivity.this.recyclerView.setLayoutManager(new LinearLayoutManager(RoutesActivity.this));
            RoutesActivity.this.recyclerView.setAdapter(routesAdapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ((TransitApplication) this.getApplicationContext()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_routes);

        this.loadingOverlay = findViewById(R.id.loading_overlay);

        this.recyclerView = this.findViewById(R.id.routes_bottom_sheet_list);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        this.routeViewModel.getRoutesLiveData().observe(RoutesActivity.this, this.routesListUpdateObserver);
        this.mapFragment = RoutesMapFragment.newInstance();
        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.routes_map_container, this.mapFragment)
                .commit();

        Toolbar toolbar = this.findViewById(R.id.map_toolbar);
        this.setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        Location startLocation = (Location) this.getIntent().getSerializableExtra(START_POINT);
        Location endLocation = (Location) this.getIntent().getSerializableExtra(END_POINT);

        if (startLocation != null && endLocation != null) {
            TextView startPoint = this.findViewById(R.id.start_point_text);
            startPoint.setText(startLocation.getName());
            TextView endPoint = this.findViewById(R.id.end_point_text);
            endPoint.setText(endLocation.getName());

            ImageView filterIcon = this.findViewById(R.id.filter_icon);
            filterIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(RoutesActivity.this, "Filter", Toast.LENGTH_SHORT).show();
                }
            });

            final long startMS = System.currentTimeMillis();
            new RouteSearchAsyncTask(startLocation, endLocation, searchService, new TaskListener() {
                @Override
                public void onFinished(Object result) {
                    long endMS = System.currentTimeMillis();
                    Log.i("SPENT TIME", String.valueOf(endMS - startMS));
                    loadingOverlay.setVisibility(View.GONE);
                }
            }).execute();
        }
    }

    @Override
    public void onItemClick(int position) {
        Route route = this.routeViewModel.getRoutesLiveData().getValue().get(position);
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        this.mapFragment.clearMap();
        Stop firstStop = null;
        for (RoutePart part : route.getParts()) {
            if (BUS == part.getTravelType()) {
                for (Stop stop : part.getStops()) {
                    if (firstStop == null) {
                        firstStop = stop;
                    }
                    polylineOptions.add(new LatLng(stop.getLocation().getLatitude(), stop.getLocation().getLongitude()));
                    this.mapFragment.addStopMarker(stop);
                }
            }
        }
        if (firstStop != null) {
            this.mapFragment.zoomOnLocation(firstStop.getLocation().getLatitude(), firstStop.getLocation().getLongitude());
            //...
        }
        this.mapFragment.addPolyline(polylineOptions);
        this.mapFragment.setSelectedRoute(route);
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }
}
