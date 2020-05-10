package org.mad.transit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.adapters.SingleLineAdapter;
import org.mad.transit.fragments.StopsMapFragment;
import org.mad.transit.model.Line;
import org.mad.transit.model.SingleLineViewModel;
import org.mad.transit.model.Stop;
import org.mad.transit.model.StopsFragmentViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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

public class SingleLineActivity extends AppCompatActivity implements SingleLineAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private SingleLineAdapter singleLineAdapter;
    private SingleLineViewModel singleLineViewModel;
    private StopsFragmentViewModel stopsFragmentViewModel;
    private StopsMapFragment stopsMapFragment;
    public static final String LINE_KEY = "line";
    public static final String LINE_NAME = "line_name";
    public static final String LINE_NUMBER = "line_number";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_line);

        Line line = (Line) this.getIntent().getSerializableExtra(LINE_KEY);

        Toolbar toolbar = this.findViewById(R.id.single_line_toolbar);
        this.setSupportActionBar(toolbar);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        final TextView lineNumber = this.findViewById(R.id.map_line_number);
        lineNumber.setText(line.getNumber());
        final TextView lineName = this.findViewById(R.id.map_line_name);
        final String[] lineStations = line.getName().split("-");
        for (String station: lineStations){
            station.trim();
        }
        lineName.setText(lineStations[0]);

        TextView timetable = this.findViewById(R.id.timetable_button);
        timetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleLineActivity.this, TimetableActivity.class);
                intent.putExtra(SingleLineActivity.LINE_NAME, lineStations);
                intent.putExtra(SingleLineActivity.LINE_NUMBER, lineNumber.getText().toString());
                startActivity(intent);
            }
        });

        this.recyclerView = this.findViewById(R.id.single_line_bottom_sheet_list);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        this.singleLineViewModel = new ViewModelProvider(this).get(SingleLineViewModel.class);
        this.stopsFragmentViewModel = new ViewModelProvider(this).get(StopsFragmentViewModel.class);
        this.singleLineViewModel.getStopsLiveData().observe(this, this.lineStopsListUpdateObserver);

        this.stopsMapFragment = StopsMapFragment.newInstance(this.stopsFragmentViewModel);
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.single_line_stops_map_container, this.stopsMapFragment).commit();

        Switch directionSwitch = this.findViewById(R.id.direction_switch);
        directionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Collections.reverse(Arrays.asList(lineStations));
                lineName.setText(lineStations[0]);
                ArrayList<Stop> reverseStops = SingleLineActivity.this.singleLineViewModel.getStopsLiveData().getValue();
                Collections.reverse(reverseStops);
                lineStopsListUpdateObserver.onChanged(reverseStops);
            }
        });
    }

    private final Observer<ArrayList<Stop>> lineStopsListUpdateObserver = new Observer<ArrayList<Stop>>() {

        @Override
        public void onChanged(ArrayList<Stop> lineStops) {
            SingleLineActivity.this.singleLineAdapter = new SingleLineAdapter(SingleLineActivity.this, lineStops, SingleLineActivity.this);
            SingleLineActivity.this.recyclerView.setLayoutManager(new LinearLayoutManager(SingleLineActivity.this));
            SingleLineActivity.this.recyclerView.setAdapter(SingleLineActivity.this.singleLineAdapter);
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
        this.stopsMapFragment.zoomOnLocation(lineStop.getLatitude(), lineStop.getLongitude());
        this.stopsMapFragment.updateFloatingLocationButton(false);
        this.stopsMapFragment.getNearbyStopsMarkers().get(position).showInfoWindow();
    }
}
