package org.mad.transit.activities;

import android.os.Bundle;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.adapters.SingleLineAdapter;
import org.mad.transit.fragments.StopsMapFragment;
import org.mad.transit.model.Line;
import org.mad.transit.model.SingleLineViewModel;
import org.mad.transit.model.Stop;
import org.mad.transit.model.StopsFragmentViewModel;

import java.util.ArrayList;

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

        TextView lineNumber = this.findViewById(R.id.map_line_number);
        lineNumber.setText(line.getNumber());
        TextView lineName = this.findViewById(R.id.map_line_name);
        String[] lineStation = line.getName().split("-");
        lineName.setText(lineStation[0]);

        this.recyclerView = this.findViewById(R.id.single_line_bottom_sheet_list);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        this.singleLineViewModel = new ViewModelProvider(this).get(SingleLineViewModel.class);
        this.stopsFragmentViewModel = new ViewModelProvider(this).get(StopsFragmentViewModel.class);
        this.singleLineViewModel.geStopsLiveData().observe(this, this.lineStopsListUpdateObserver);

        this.stopsMapFragment = StopsMapFragment.newInstance(this.stopsFragmentViewModel);
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.single_line_stops_map_container, this.stopsMapFragment).commit();
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
        Stop lineStop = this.singleLineViewModel.geStopsLiveData().getValue().get(position);
        this.stopsMapFragment.zoomOnLocation(lineStop.getLatitude(), lineStop.getLongitude());
        this.stopsMapFragment.updateFloatingLocationButton(false);
        this.stopsMapFragment.getNearbyStopsMarkers().get(position).showInfoWindow();
    }
}
