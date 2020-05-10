package org.mad.transit.activities;

import android.os.Bundle;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.adapters.SingleLineAdapter;
import org.mad.transit.fragments.MapFragment;
import org.mad.transit.fragments.SingleLineMapFragment;
import org.mad.transit.model.Line;
import org.mad.transit.model.SingleLineViewModel;
import org.mad.transit.model.Stop;

import java.util.List;

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
    private SingleLineViewModel singleLineViewModel;
    private MapFragment mapFragment;
    public static final String LINE_KEY = "line";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_line);

        Line line = (Line) getIntent().getSerializableExtra(LINE_KEY);

        Toolbar toolbar = findViewById(R.id.single_line_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        TextView lineNumber = findViewById(R.id.map_line_number);
        lineNumber.setText(line.getNumber());
        TextView lineName = findViewById(R.id.map_line_name);
        String[] lineStation = line.getName().split("-");
        lineName.setText(lineStation[0]);

        recyclerView = findViewById(R.id.single_line_bottom_sheet_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        singleLineViewModel = new ViewModelProvider(this).get(SingleLineViewModel.class);
        singleLineViewModel.getStopsLiveData().observe(this, lineStopsListUpdateObserver);

        mapFragment = SingleLineMapFragment.newInstance(singleLineViewModel);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.single_line_stops_map_container, mapFragment).commit();
    }

    private final Observer<List<Stop>> lineStopsListUpdateObserver = new Observer<List<Stop>>() {

        @Override
        public void onChanged(List<Stop> lineStops) {
            SingleLineAdapter singleLineAdapter = new SingleLineAdapter(SingleLineActivity.this, lineStops, SingleLineActivity.this);
            recyclerView.setLayoutManager(new LinearLayoutManager(SingleLineActivity.this));
            recyclerView.setAdapter(singleLineAdapter);
        }
    };

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onItemClick(int position) {
        Stop lineStop = singleLineViewModel.getStopsLiveData().getValue().get(position);
        mapFragment.zoomOnLocation(lineStop.getLatitude(), lineStop.getLongitude());
        mapFragment.getStopMarkers().get(position).showInfoWindow();
    }
}
