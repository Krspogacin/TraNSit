package org.mad.transit.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.adapters.PastDirectionsAdapter;
import org.mad.transit.fragments.DirectionsFragment;
import org.mad.transit.model.PastDirection;
import org.mad.transit.repository.PastDirectionRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PastDirectionsActivity extends AppCompatActivity implements PastDirectionsAdapter.OnItemClickListener {

    private PastDirectionsAdapter pastDirectionsAdapter;
    private MenuItem deleteAllMenuItem;
    private boolean disableDeleteAllMenuItem;

    @Inject
    PastDirectionRepository pastDirectionRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ((TransitApplication) this.getApplicationContext()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_past_directions);

        this.pastDirectionsAdapter = new PastDirectionsAdapter(this, this);
        RecyclerView recyclerView = this.findViewById(R.id.past_directions_list);
        recyclerView.setAdapter(this.pastDirectionsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Show the Up button in the action bar.
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<PastDirection> pastDirections = this.pastDirectionRepository.findAll();
        this.pastDirectionsAdapter.setPastDirections(pastDirections);

        if (this.deleteAllMenuItem == null) {
            if (pastDirections.isEmpty()) {
                this.disableDeleteAllMenuItem = true;
            }
        } else {
            if (pastDirections.isEmpty()) {
                this.deleteAllMenuItem.setEnabled(false);
            } else {
                this.deleteAllMenuItem.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.menu_remove_all, menu);

        this.deleteAllMenuItem = menu.findItem(R.id.action_remove_all);

        if (this.disableDeleteAllMenuItem) {
            this.deleteAllMenuItem.setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == R.id.action_remove_all) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.past_directions_lines_dialog_title)
                    .setMessage(R.string.past_directions_lines_dialog_message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            PastDirectionsActivity.this.pastDirectionRepository.deleteAll();
                            PastDirectionsActivity.this.pastDirectionsAdapter.setPastDirections(new ArrayList<PastDirection>());
                            PastDirectionsActivity.this.deleteAllMenuItem.setEnabled(false);
                            View view = PastDirectionsActivity.this.findViewById(android.R.id.content);
                            final Snackbar snackbar = Snackbar.make(view, R.string.past_directions_removed_message, Snackbar.LENGTH_SHORT);
                            snackbar.setAction(R.string.dismiss_snack_bar, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }

    @Override
    public void onItemClick(int position) {
        PastDirection pastDirection = this.pastDirectionsAdapter.getPastDirections().get(position);

        Intent intent = new Intent(this, RoutesActivity.class);
        intent.putExtra(DirectionsFragment.START_POINT, pastDirection.getStartLocation().getName());
        intent.putExtra(DirectionsFragment.END_POINT, pastDirection.getEndLocation().getName());
        this.startActivity(intent);

        this.pastDirectionRepository.update(pastDirection);
    }
}