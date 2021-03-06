package org.mad.transit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.adapters.SingleLineAdapter;
import org.mad.transit.fragments.SingleLineMapFragment;
import org.mad.transit.model.Line;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.LineOneDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.repository.LineRepository;
import org.mad.transit.view.model.SingleLineViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SingleLineActivity extends AppCompatActivity implements SingleLineAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private SingleLineMapFragment mapFragment;
    public static final String LINE_KEY = "line";
    public static final String LINE_NAME_KEY = "line_name";
    public static final String DIRECTION_KEY = "direction";
    public static final String FAVOURITE_LINES_KEY = "favourite_lines";
    private SharedPreferences defaultSharedPreferences;
    private Line line;
    private LineDirection currentDirection;
    private FrameLayout loadingOverlay;

    @Inject
    SingleLineViewModel singleLineViewModel;

    @Inject
    LineRepository lineRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        ((TransitApplication) this.getApplicationContext()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.single_line);

        this.loadingOverlay = this.findViewById(R.id.loading_overlay);

        this.defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        this.line = (Line) this.getIntent().getSerializableExtra(LINE_KEY);

        Toolbar toolbar = this.findViewById(R.id.single_line_toolbar);
        this.setSupportActionBar(toolbar);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        boolean bExist = this.lineRepository.doesDirectionBExists(this.line.getId());
        this.singleLineViewModel.getStopsLiveData().setValue(new ArrayList<Stop>());
        this.singleLineViewModel.setLineLocations(new ArrayList<Location>());
        this.singleLineViewModel.findAllStopsAndLocationsByLineIdAndLineDirection(this.line.getId(), LineDirection.A);

        TextView lineNumber = this.findViewById(R.id.map_line_number);
        lineNumber.setText(this.line.getNumber());
        final TextView lineName = this.findViewById(R.id.map_line_name);
        final String[] lineStations = this.line.getTitle().split("-");
        lineName.setText(lineStations[0].trim());

        TextView timetable = this.findViewById(R.id.timetable_button);
        timetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleLineActivity.this, TimetableActivity.class);
                intent.putExtra(SingleLineActivity.LINE_NAME_KEY, lineStations);
                intent.putExtra(SingleLineActivity.LINE_KEY, SingleLineActivity.this.line);
                intent.putExtra(SingleLineActivity.DIRECTION_KEY, SingleLineActivity.this.currentDirection);
                SingleLineActivity.this.startActivity(intent);
            }
        });

        this.recyclerView = this.findViewById(R.id.single_line_bottom_sheet_list);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(SingleLineActivity.this));
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        this.singleLineViewModel.getStopsLiveData().observe(this, this.lineStopsListUpdateObserver);

        this.mapFragment = SingleLineMapFragment.newInstance();
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.single_line_stops_map_container, this.mapFragment).commit();

        Switch directionSwitch = this.findViewById(R.id.direction_switch);
        if (!bExist) {
            directionSwitch.setEnabled(false);
            directionSwitch.setClickable(false);
        } else {
            directionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Collections.reverse(Arrays.asList(lineStations));
                    lineName.setText(lineStations[0]);
                    if (SingleLineActivity.this.currentDirection == LineDirection.A) {
                        if (SingleLineActivity.this.line.getLineDirectionB() == null) {
                            SingleLineActivity.this.singleLineViewModel.findAllStopsAndLocationsByLineIdAndLineDirection(SingleLineActivity.this.line.getId(), LineDirection.B);
                            if (SingleLineActivity.this.loadingOverlay.getVisibility() == View.GONE) {
                                SingleLineActivity.this.loadingOverlay.setVisibility(View.VISIBLE);
                            }
                        } else {
                            SingleLineActivity.this.singleLineViewModel.getStopsLiveData().setValue(SingleLineActivity.this.line.getLineDirectionB().getStops());
                            SingleLineActivity.this.singleLineViewModel.setLineLocations(SingleLineActivity.this.line.getLineDirectionB().getLocations());
                        }
                    } else {
                        if (SingleLineActivity.this.line.getLineDirectionA() == null) {
                            SingleLineActivity.this.singleLineViewModel.findAllStopsAndLocationsByLineIdAndLineDirection(SingleLineActivity.this.line.getId(), LineDirection.A);
                            if (SingleLineActivity.this.loadingOverlay.getVisibility() == View.GONE) {
                                SingleLineActivity.this.loadingOverlay.setVisibility(View.VISIBLE);
                            }
                        } else {
                            SingleLineActivity.this.singleLineViewModel.getStopsLiveData().setValue(SingleLineActivity.this.line.getLineDirectionA().getStops());
                            SingleLineActivity.this.singleLineViewModel.setLineLocations(SingleLineActivity.this.line.getLineDirectionA().getLocations());
                        }
                    }
                }
            });
        }

        final ImageView favouritesImageView = this.findViewById(R.id.favorites_icon);
        favouritesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<String> favouriteLines = new HashSet<>(SingleLineActivity.this.defaultSharedPreferences.getStringSet(SingleLineActivity.FAVOURITE_LINES_KEY, new HashSet<String>()));
                String number = SingleLineActivity.this.line.getNumber();
                if (favouriteLines.contains(number)) {
                    favouriteLines.remove(number);
                    favouritesImageView.setImageResource(R.drawable.ic_star_border_primary_24dp);
                    View view = SingleLineActivity.this.findViewById(android.R.id.content);
                    final Snackbar snackbar = Snackbar.make(view, SingleLineActivity.this.getString(R.string.favourite_line_added_message, number), Snackbar.LENGTH_SHORT);
                    snackbar.setAction(R.string.dismiss_snack_bar, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                } else {
                    favouriteLines.add(number);
                    favouritesImageView.setImageResource(R.drawable.ic_star_primary_24dp);
                    View view = SingleLineActivity.this.findViewById(android.R.id.content);
                    final Snackbar snackbar = Snackbar.make(view, SingleLineActivity.this.getString(R.string.favourite_line_deleted_message, number), Snackbar.LENGTH_SHORT);
                    snackbar.setAction(R.string.dismiss_snack_bar, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }

                SingleLineActivity.this.defaultSharedPreferences.edit().putStringSet(SingleLineActivity.FAVOURITE_LINES_KEY, favouriteLines).apply();

                favouritesImageView.setEnabled(false);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        favouritesImageView.setEnabled(true);
                    }
                }, 2000);
            }
        });

        Set<String> favouriteLines = this.defaultSharedPreferences.getStringSet(FAVOURITE_LINES_KEY, new HashSet<String>());
        if (favouriteLines.contains(this.line.getNumber())) {
            favouritesImageView.setImageResource(R.drawable.ic_star_primary_24dp);
        }
    }

    private final Observer<List<Stop>> lineStopsListUpdateObserver = lineStops -> {
        SingleLineAdapter singleLineAdapter = new SingleLineAdapter(SingleLineActivity.this, lineStops, SingleLineActivity.this);
        SingleLineActivity.this.recyclerView.setAdapter(singleLineAdapter);
        if (!SingleLineActivity.this.singleLineViewModel.getStopsLiveData().getValue().isEmpty()) {
            if (SingleLineActivity.this.loadingOverlay.getVisibility() == View.VISIBLE) {
                SingleLineActivity.this.loadingOverlay.setVisibility(View.GONE);
            }
            if (SingleLineActivity.this.currentDirection == LineDirection.A) {
                if (SingleLineActivity.this.line.getLineDirectionB() == null) {
                    LineOneDirection directionB = new LineOneDirection(SingleLineActivity.this.currentDirection, SingleLineActivity.this.singleLineViewModel.getStopsLiveData().getValue(), SingleLineActivity.this.singleLineViewModel.getLineLocations());
                    SingleLineActivity.this.line.setLineDirectionB(directionB);
                }
                SingleLineActivity.this.refreshMap();
            } else if (SingleLineActivity.this.currentDirection == LineDirection.B) {
                if (SingleLineActivity.this.line.getLineDirectionA() == null) {
                    LineOneDirection directionA = new LineOneDirection(SingleLineActivity.this.currentDirection, SingleLineActivity.this.singleLineViewModel.getStopsLiveData().getValue(), SingleLineActivity.this.singleLineViewModel.getLineLocations());
                    SingleLineActivity.this.line.setLineDirectionA(directionA);
                }
                SingleLineActivity.this.refreshMap();
            } else {
                SingleLineActivity.this.currentDirection = LineDirection.A;
                LineOneDirection directionA = new LineOneDirection(SingleLineActivity.this.currentDirection, SingleLineActivity.this.singleLineViewModel.getStopsLiveData().getValue(), SingleLineActivity.this.singleLineViewModel.getLineLocations());
                SingleLineActivity.this.line.setLineDirectionA(directionA);
                SingleLineActivity.this.mapFragment.addStartMarkersAndPolyline();
            }
        }
    };

    private void refreshMap() {
        if (this.currentDirection == LineDirection.A) {
            this.mapFragment.clearMap();
            for (Stop stop : this.line.getLineDirectionB().getStops()) {
                this.mapFragment.addStopMarker(stop);
            }
            this.mapFragment.setPolyLineOnMap(this.line.getLineDirectionB().getLocations());
            this.mapFragment.zoomOnLocation(this.line.getLineDirectionB().getStops().get(0).getLocation().getLatitude(), this.line.getLineDirectionB().getStops().get(0).getLocation().getLongitude());
            this.currentDirection = LineDirection.B;
        } else {
            this.mapFragment.clearMap();
            for (Stop stop : this.line.getLineDirectionA().getStops()) {
                this.mapFragment.addStopMarker(stop);
            }
            this.mapFragment.setPolyLineOnMap(SingleLineActivity.this.line.getLineDirectionA().getLocations());
            this.mapFragment.zoomOnLocation(this.line.getLineDirectionA().getStops().get(0).getLocation().getLatitude(), this.line.getLineDirectionA().getStops().get(0).getLocation().getLongitude());
            this.currentDirection = LineDirection.A;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }

    @Override
    public void onItemClick(int position) {
        Stop lineStop = this.singleLineViewModel.getStopsLiveData().getValue().get(position);
        this.mapFragment.zoomOnLocation(lineStop.getLocation().getLatitude(), lineStop.getLocation().getLongitude());
        this.mapFragment.getStopMarkers().get(position).showInfoWindow();
    }
}
