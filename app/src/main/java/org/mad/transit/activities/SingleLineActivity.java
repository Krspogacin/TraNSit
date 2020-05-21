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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.mad.transit.R;
import org.mad.transit.adapters.SingleLineAdapter;
import org.mad.transit.fragments.SingleLineMapFragment;
import org.mad.transit.model.Line;
import org.mad.transit.model.SingleLineViewModel;
import org.mad.transit.model.Stop;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SingleLineActivity extends AppCompatActivity implements SingleLineAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private SingleLineViewModel singleLineViewModel;
    private SingleLineMapFragment mapFragment;
    public static final String LINE_KEY = "line";
    public static final String LINE_NAME = "line_name";
    public static final String LINE_NUMBER = "line_number";
    public static final String FAVOURITE_LINES_KEY = "favourite_lines";
    private SharedPreferences sharedPreferences;
    private Line line;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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

        final TextView lineNumber = this.findViewById(R.id.map_line_number);
        lineNumber.setText(this.line.getNumber());
        final TextView lineName = this.findViewById(R.id.map_line_name);
        final String[] lineStations = this.line.getTitle().split("-");
        lineName.setText(lineStations[0].trim());

        TextView timetable = this.findViewById(R.id.timetable_button);
        timetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleLineActivity.this, TimetableActivity.class);
                intent.putExtra(SingleLineActivity.LINE_NAME, lineStations);
                intent.putExtra(SingleLineActivity.LINE_NUMBER, lineNumber.getText().toString());
                SingleLineActivity.this.startActivity(intent);
            }
        });

        this.recyclerView = this.findViewById(R.id.single_line_bottom_sheet_list);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        this.singleLineViewModel = new ViewModelProvider(this).get(SingleLineViewModel.class);
        this.singleLineViewModel.getStopsLiveData().observe(this, this.lineStopsListUpdateObserver);

        this.mapFragment = SingleLineMapFragment.newInstance(this.singleLineViewModel);
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.single_line_stops_map_container, this.mapFragment).commit();

        Switch directionSwitch = this.findViewById(R.id.direction_switch);
        directionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Collections.reverse(Arrays.asList(lineStations));
                lineName.setText(lineStations[0]);
                if (SingleLineActivity.this.singleLineViewModel.getStopsLiveData().getValue() != null) {
                    List<Stop> reverseStops = SingleLineActivity.this.singleLineViewModel.getStopsLiveData().getValue();
                    Collections.reverse(reverseStops);
                    SingleLineActivity.this.lineStopsListUpdateObserver.onChanged(reverseStops);
                    SingleLineActivity.this.mapFragment.getStopMarkers().clear();
                    for (Stop stop : reverseStops) {
                        SingleLineActivity.this.mapFragment.addStopMarker(stop);
                    }
                    SingleLineActivity.this.mapFragment.zoomOnLocation(reverseStops.get(0).getLocation().getLatitude(), reverseStops.get(0).getLocation().getLongitude());
                }
            }
        });

        final ImageView favouritesImageView = this.findViewById(R.id.favorites_icon);
        favouritesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<String> favouriteLines = new HashSet<>(SingleLineActivity.this.sharedPreferences.getStringSet(SingleLineActivity.FAVOURITE_LINES_KEY, new HashSet<String>()));
                String number = SingleLineActivity.this.line.getNumber();
                if (favouriteLines.contains(number)) {
                    favouriteLines.remove(number);
                    favouritesImageView.setImageResource(R.drawable.ic_star_border_primary_24dp);
                    Toast.makeText(SingleLineActivity.this, "Linija " + number + " je uklonjena iz vaših omiljenih linija", Toast.LENGTH_SHORT).show();
                } else {
                    favouriteLines.add(number);
                    favouritesImageView.setImageResource(R.drawable.ic_star_primary_24dp);
                    Toast.makeText(SingleLineActivity.this, "Linija " + number + " je dodata u vaše omiljene linije", Toast.LENGTH_SHORT).show();
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
            SingleLineActivity.this.recyclerView.setLayoutManager(new LinearLayoutManager(SingleLineActivity.this));
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
