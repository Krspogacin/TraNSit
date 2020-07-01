package org.mad.transit.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.adapters.RoutesAdapter;
import org.mad.transit.dto.ActionDto;
import org.mad.transit.dto.RouteDto;
import org.mad.transit.fragments.RoutesMapFragment;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.search.RouteSortKey;
import org.mad.transit.search.SearchOptions;
import org.mad.transit.search.SearchService;
import org.mad.transit.util.Constants;
import org.mad.transit.view.model.RouteViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import static org.mad.transit.fragments.DirectionsFragment.END_POINT;
import static org.mad.transit.fragments.DirectionsFragment.SEARCH_OPTIONS;
import static org.mad.transit.fragments.DirectionsFragment.START_POINT;
import static org.mad.transit.search.RouteComparator.getComparatorBySortKey;
import static org.mad.transit.search.RouteComparator.getDefaultSortKey;

public class RoutesActivity extends AppCompatActivity implements RoutesAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private RoutesMapFragment mapFragment;
    private FrameLayout loadingOverlay;
    private RouteViewModel routeViewModel;
    private Location startLocation;
    private Location endLocation;
    private RouteSortKey sortKey;

    @Inject
    SearchService searchService;

    private final Observer<List<RouteDto>> routesListUpdateObserver = routes -> {
        RoutesAdapter routesAdapter = new RoutesAdapter(RoutesActivity.this, routes, RoutesActivity.this);
        RoutesActivity.this.recyclerView.setLayoutManager(new LinearLayoutManager(RoutesActivity.this));
        RoutesActivity.this.recyclerView.setAdapter(routesAdapter);
        if (routes != null) {
            RoutesActivity.this.loadingOverlay.setVisibility(View.GONE);
            RoutesActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            RoutesActivity.this.mapFragment.expandBottomSheet();
            this.mapFragment.setViewsPadding();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ((TransitApplication) this.getApplicationContext()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_routes);

        this.loadingOverlay = this.findViewById(R.id.loading_overlay);

        this.recyclerView = this.findViewById(R.id.routes_bottom_sheet_list);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        this.routeViewModel = new RouteViewModel(this.searchService);
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

        this.sortKey = getDefaultSortKey();
        this.startLocation = (Location) this.getIntent().getSerializableExtra(START_POINT);
        this.endLocation = (Location) this.getIntent().getSerializableExtra(END_POINT);
        SearchOptions searchOptions = (SearchOptions) this.getIntent().getSerializableExtra(SEARCH_OPTIONS);

        if (this.startLocation != null && this.endLocation != null) {
            TextView startPoint = this.findViewById(R.id.start_point_text);
            startPoint.setText(this.startLocation.getName());
            TextView endPoint = this.findViewById(R.id.end_point_text);
            endPoint.setText(this.endLocation.getName());

            ImageView filterIcon = this.findViewById(R.id.filter_icon);
            filterIcon.setOnClickListener(v -> {

                List<RouteSortKey> routeSortKeys = Arrays.asList(RouteSortKey.values());
                List<String> sortItems = new ArrayList<>();
                for (RouteSortKey routeSortKey : routeSortKeys) {
                    sortItems.add(this.getString(routeSortKey.getLabelId()));
                }
                ListAdapter listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, sortItems);
                int selectedItem = this.sortKey != null ? routeSortKeys.indexOf(this.sortKey) : 0;

                new AlertDialog.Builder(this)
                        .setTitle(R.string.sort_dialog_title)
                        .setNegativeButton(R.string.dismiss_snack_bar, (dialog, which) -> {
                        })
                        .setPositiveButton(R.string.apply_sort_button_lable, (dialog, which) -> this.routeViewModel.sortRoutes(getComparatorBySortKey(this.sortKey)))
                        .setSingleChoiceItems(listAdapter, selectedItem, (dialog, selectedIndex) -> this.sortKey = RouteSortKey.values()[selectedIndex])
                        .show();
            });

            this.routeViewModel.findRoutes(this.startLocation, this.endLocation, searchOptions);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            this.mapFragment.setStartLocation(this.startLocation);
            this.mapFragment.setEndLocation(this.endLocation);
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
        LatLngBounds.Builder routeBoundsBuilder = LatLngBounds.builder();
        for (Pair<Long, LineDirection> key : route.getPath().keySet()) {
            List<Location> pathLocations = route.getPath().get(key);
            if (pathLocations != null) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(Constants.getLineColor(key.first));

                for (Location pathLocation : pathLocations) {
                    LatLng latLng = new LatLng(pathLocation.getLatitude(), pathLocation.getLongitude());
                    polylineOptions.add(latLng);
                    routeBoundsBuilder.include(latLng);
                }
                this.mapFragment.addPolyline(polylineOptions);
            }
        }
        if (route.getPath().isEmpty()) {
            // for walk routes include start and end locations in zoom
            routeBoundsBuilder.include(new LatLng(this.startLocation.getLatitude(), this.startLocation.getLongitude()));
            routeBoundsBuilder.include(new LatLng(this.endLocation.getLatitude(), this.endLocation.getLongitude()));
        }
        this.mapFragment.setSelectedRoute(route);
        this.mapFragment.zoomOnRoute(routeBoundsBuilder.build(), 100);
        this.mapFragment.addLocationMarker(this.startLocation);
        this.mapFragment.addLocationMarker(this.endLocation);
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }
}
