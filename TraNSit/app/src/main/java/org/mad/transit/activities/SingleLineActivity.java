package org.mad.transit.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

public class SingleLineActivity extends AppCompatActivity implements SingleLineAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private SingleLineMapFragment mapFragment;
    public static final String LINE_KEY = "line";
    public static final String LINE_NAME_KEY = "line_name";
    public static final String DIRECTION_KEY = "direction";
    public static final String FAVOURITE_LINES_KEY = "favourite_lines";
    private SharedPreferences sharedPreferences;
    private Line line;
    private LineDirection currentDirection;

    @Inject
    SingleLineViewModel singleLineViewModel;

    @Inject
    LineRepository lineRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        ((TransitApplication) this.getApplicationContext()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.single_line);

        this.sharedPreferences = this.getSharedPreferences(this.getString(R.string.favourites_preference_file_key), Context.MODE_PRIVATE);

        this.line = (Line) this.getIntent().getSerializableExtra(LINE_KEY);

        Toolbar toolbar = this.findViewById(R.id.single_line_toolbar);
        this.setSupportActionBar(toolbar);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        this.currentDirection = LineDirection.A;
        boolean bExist = this.lineRepository.doesDirectionBExists(this.line.getId());
        List<Stop> stops = this.singleLineViewModel.findAllStopsByLineIdAndLineDirection(this.line.getId(), this.currentDirection);
        this.singleLineViewModel.getStopsLiveData().setValue(stops);
        List<Location> locations = this.singleLineViewModel.findAllLocationsByLineIdAndLineDirection(this.line.getId(), this.currentDirection);
        LineOneDirection directionA = new LineOneDirection(this.currentDirection, stops, locations);
        this.line.setLineDirectionA(directionA);

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
                            List<Stop> stops = SingleLineActivity.this.singleLineViewModel.findAllStopsByLineIdAndLineDirection(SingleLineActivity.this.line.getId(), LineDirection.B);
                            List<Location> locations = SingleLineActivity.this.singleLineViewModel.findAllLocationsByLineIdAndLineDirection(SingleLineActivity.this.line.getId(), LineDirection.B);
                            LineOneDirection directionB = new LineOneDirection(SingleLineActivity.this.currentDirection, stops, locations);
                            SingleLineActivity.this.line.setLineDirectionB(directionB);
                        }
                        SingleLineActivity.this.singleLineViewModel.getStopsLiveData().setValue(SingleLineActivity.this.line.getLineDirectionB().getStops());
                        SingleLineActivity.this.mapFragment.clearMap();
                        for (Stop stop : SingleLineActivity.this.line.getLineDirectionB().getStops()) {
                            SingleLineActivity.this.mapFragment.addStopMarker(stop);
                        }
                        SingleLineActivity.this.mapFragment.setPolyLineOnMap(SingleLineActivity.this.line.getLineDirectionB().getLocations());
                        SingleLineActivity.this.currentDirection = LineDirection.B;
                        //mapFragment.zoomOnLocation(line.getLineDirectionB().getStops().get(0).getLocation().getLatitude(), line.getLineDirectionB().getStops().get(0).getLocation().getLongitude());
                    } else {
                        if (SingleLineActivity.this.line.getLineDirectionA() == null) {
                            List<Stop> stops = SingleLineActivity.this.singleLineViewModel.findAllStopsByLineIdAndLineDirection(SingleLineActivity.this.line.getId(), LineDirection.A);
                            List<Location> locations = SingleLineActivity.this.singleLineViewModel.findAllLocationsByLineIdAndLineDirection(SingleLineActivity.this.line.getId(), LineDirection.A);
                            LineOneDirection directionA = new LineOneDirection(SingleLineActivity.this.currentDirection, stops, locations);
                            SingleLineActivity.this.line.setLineDirectionA(directionA);
                        }
                        SingleLineActivity.this.singleLineViewModel.getStopsLiveData().setValue(SingleLineActivity.this.line.getLineDirectionA().getStops());
                        SingleLineActivity.this.mapFragment.clearMap();
                        for (Stop stop : SingleLineActivity.this.line.getLineDirectionA().getStops()) {
                            SingleLineActivity.this.mapFragment.addStopMarker(stop);
                        }
                        SingleLineActivity.this.mapFragment.setPolyLineOnMap(SingleLineActivity.this.line.getLineDirectionA().getLocations());
                        SingleLineActivity.this.currentDirection = LineDirection.A;
                        //mapFragment.zoomOnLocation(line.getLineDirectionB().getStops().get(0).getLocation().getLatitude(), line.getLineDirectionB().getStops().get(0).getLocation().getLongitude());
                    }
                }
            });
        }

        final ImageView favouritesImageView = this.findViewById(R.id.favorites_icon);
        favouritesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<String> favouriteLines = new HashSet<>(SingleLineActivity.this.sharedPreferences.getStringSet(SingleLineActivity.FAVOURITE_LINES_KEY, new HashSet<String>()));
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

                SingleLineActivity.this.sharedPreferences.edit().putStringSet(SingleLineActivity.FAVOURITE_LINES_KEY, favouriteLines).apply();

                favouritesImageView.setEnabled(false);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        favouritesImageView.setEnabled(true);
                    }
                }, 2000);
            }
        });

        Set<String> favouriteLines = this.sharedPreferences.getStringSet(FAVOURITE_LINES_KEY, new HashSet<String>());
        if (favouriteLines.contains(this.line.getNumber())) {
            favouritesImageView.setImageResource(R.drawable.ic_star_primary_24dp);
        }
    }

    private final Observer<List<Stop>> lineStopsListUpdateObserver = new Observer<List<Stop>>() {

        @Override
        public void onChanged(List<Stop> lineStops) {
            SingleLineAdapter singleLineAdapter = new SingleLineAdapter(SingleLineActivity.this, lineStops, SingleLineActivity.this);
            SingleLineActivity.this.recyclerView.setAdapter(singleLineAdapter);
        }
    };

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
