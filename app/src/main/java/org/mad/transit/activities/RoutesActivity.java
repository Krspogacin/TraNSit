package org.mad.transit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.mad.transit.R;
import org.mad.transit.adapters.RoutesAdapter;
import org.mad.transit.fragments.MapFragment;
import org.mad.transit.fragments.RoutesMapFragment;
import org.mad.transit.model.Route;
import org.mad.transit.model.RoutesViewModel;

import java.util.List;

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
            RoutesActivity.this.recyclerView.setLayoutManager(new LinearLayoutManager(RoutesActivity.this));
            RoutesActivity.this.recyclerView.setAdapter(routesAdapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_routes);

        this.recyclerView = this.findViewById(R.id.routes_bottom_sheet_list);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        this.routesViewModel = new ViewModelProvider(this).get(RoutesViewModel.class);
        this.routesViewModel.getRoutesLiveData().observe(RoutesActivity.this, this.routesListUpdateObserver);
        this.mapFragment = RoutesMapFragment.newInstance(this.routesViewModel);
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

        String startPointText = this.getIntent().getStringExtra(START_POINT);
        String endPointText = this.getIntent().getStringExtra(END_POINT);

        TextView startPoint = this.findViewById(R.id.start_point_text);
        startPoint.setText(startPointText);
        TextView endPoint = this.findViewById(R.id.end_point_text);
        endPoint.setText(endPointText);
    }

    @Override
    public void onItemClick(int position) {
//        Route route = routesViewModel.getRoutesLiveData().getValue().get(position);
//        stopsMapFragment.zoomOnLocation(nearbyStop.getLatitude(), nearbyStop.getLongitude());
//        stopsMapFragment.updateFloatingLocationButton(false);
//        stopsMapFragment.getNearbyStopsMarkers().get(position).showInfoWindow();
        Intent intent = new Intent(this, NavigationActivity.class);
        intent.putExtra(NavigationActivity.ROUTE, this.routesViewModel.getRoutesLiveData().getValue().get(position));
        this.startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }
}
