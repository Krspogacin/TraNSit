package org.mad.transit.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.adapters.RoutesAdapter;
import org.mad.transit.dto.ActionDto;
import org.mad.transit.dto.RouteDto;
import org.mad.transit.fragments.RoutesMapFragment;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.search.SearchService;
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

public class RoutesActivity extends AppCompatActivity implements RoutesAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private RoutesMapFragment mapFragment;
    private FrameLayout loadingOverlay;
    private RouteViewModel routeViewModel;
    private Location startLocation;
    private Location endLocation;

    @Inject
    SearchService searchService;

    private final Observer<List<RouteDto>> routesListUpdateObserver = routes -> {
        RoutesAdapter routesAdapter = new RoutesAdapter(RoutesActivity.this, routes, RoutesActivity.this);
        RoutesActivity.this.recyclerView.setLayoutManager(new LinearLayoutManager(RoutesActivity.this));
        RoutesActivity.this.recyclerView.setAdapter(routesAdapter);
        if (routes != null) {
            loadingOverlay.setVisibility(View.GONE);
            RoutesActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            RoutesActivity.this.mapFragment.expandBottomSheet();
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

        this.routeViewModel = new RouteViewModel(searchService);
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

        startLocation = (Location) this.getIntent().getSerializableExtra(START_POINT);
        endLocation = (Location) this.getIntent().getSerializableExtra(END_POINT);

        if (startLocation != null && endLocation != null) {
            TextView startPoint = this.findViewById(R.id.start_point_text);
            startPoint.setText(startLocation.getName());
            TextView endPoint = this.findViewById(R.id.end_point_text);
            endPoint.setText(endLocation.getName());

            ImageView filterIcon = this.findViewById(R.id.filter_icon);
            filterIcon.setOnClickListener(v -> Toast.makeText(RoutesActivity.this, "Filter", Toast.LENGTH_SHORT).show());

            routeViewModel.findRoutes(startLocation, endLocation);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            this.mapFragment.setStartLocation(startLocation);
            this.mapFragment.setEndLocation(endLocation);
        }
    }

    @Override
    public void onItemClick(int position) {
        RouteDto route = this.routeViewModel.getRoutesLiveData().getValue().get(position);
        this.mapFragment.clearMap();
        for (ActionDto action : route.getActions()) {
            Stop stop = action.getStop();
            if (stop != null) {
                this.mapFragment.addStopMarker(stop);
            }
        }
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        LatLngBounds.Builder routeBoundsBuilder = LatLngBounds.builder();
        for (Location pathLocation : route.getPath()) {
            LatLng latLng = new LatLng(pathLocation.getLatitude(), pathLocation.getLongitude());
            polylineOptions.add(latLng);
            routeBoundsBuilder.include(latLng);
        }
        this.mapFragment.addPolyline(polylineOptions);
        this.mapFragment.setSelectedRoute(route);
        if (!route.getPath().isEmpty()) {
            this.mapFragment.zoomOnRoute(routeBoundsBuilder.build());
        }
        this.mapFragment.addLocationMarker(startLocation);
        this.mapFragment.addLocationMarker(endLocation);
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }
}
