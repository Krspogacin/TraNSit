package org.mad.transit.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import org.mad.transit.R;
import org.mad.transit.adapters.TimetableTabAdapter;
import org.mad.transit.model.Line;
import org.mad.transit.model.LineDirection;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

public class TimetableActivity extends AppCompatActivity {

    public static final String LINE_NAME = "line_name";
    public static final String LINE_KEY = "line";
    public static final String DIRECTION_KEY = "direction";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_activity);

        String[] lineNameList = (String[]) this.getIntent().getSerializableExtra(LINE_NAME);
        String lineName = "";
        for (int i = 0; i < lineNameList.length; i++){
            lineName += lineNameList[i];
            if (i != lineNameList.length-1){
                lineName += " - ";
            }
        }
        Line line = (Line) this.getIntent().getSerializableExtra(LINE_KEY);
        LineDirection direction = (LineDirection) this.getIntent().getSerializableExtra(DIRECTION_KEY);

        Toolbar toolbar = findViewById(R.id.timetable_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        TextView lineNumberView = this.findViewById(R.id.timetable_line_number);
        lineNumberView.setText(line.getNumber());
        TextView lineNameView = this.findViewById(R.id.toolbar_title);
        lineNameView.setText(lineName);

        TimetableTabAdapter timetableTabAdapter = new TimetableTabAdapter(this, getSupportFragmentManager(), line.getId(), direction);
        ViewPager viewPager = findViewById(R.id.timetable_view_pager);
        viewPager.setAdapter(timetableTabAdapter);
        TabLayout tabs = findViewById(R.id.timetable_tabs);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }
}
