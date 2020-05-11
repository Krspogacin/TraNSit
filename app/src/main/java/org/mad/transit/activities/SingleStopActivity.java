package org.mad.transit.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import org.mad.transit.R;
import org.mad.transit.fragments.StopLinesFragment;
import org.mad.transit.model.Stop;

public class SingleStopActivity extends AppCompatActivity {

    public static final String STOP_KEY = "stop";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_single_stop);

        Stop stop = (Stop) this.getIntent().getSerializableExtra(STOP_KEY);

        if (stop == null) {
            Toast.makeText(this, "Chosen stop couldn't be displayed.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toolbar toolbar = this.findViewById(R.id.single_stop_toolbar);
        this.setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        TextView stopNameTextView = this.findViewById(R.id.single_stop_name);
        stopNameTextView.setText(stop.getTitle());

        StopLinesFragment stopLinesFragment = StopLinesFragment.newInstance(stop);
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.single_stop_list_container, stopLinesFragment).commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }
}