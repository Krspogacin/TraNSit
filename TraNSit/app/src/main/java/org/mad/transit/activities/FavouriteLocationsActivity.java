package org.mad.transit.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.adapters.FavouritePlacesAdapter;
import org.mad.transit.model.FavouriteLocation;
import org.mad.transit.model.Location;
import org.mad.transit.repository.FavouriteLocationRepository;
import org.mad.transit.repository.LocationRepository;
import org.mad.transit.util.SwipeToDeleteCallback;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FavouriteLocationsActivity extends AppCompatActivity {

    public static int ADD_FAVOURITE_LOCATION_CODE = 9876;
    public static int SHOW_AND_SAVE_FAVOURITE_LOCATION_CODE = 6789;
    public static String FAVOURITE_LOCATION_KEY = "favourite_location";
    private FavouritePlacesAdapter favouritePlacesAdapter;
    private MenuItem deleteAllMenuItem;
    private boolean disableDeleteAllMenuItem;

    @Inject
    FavouriteLocationRepository favouriteLocationRepository;

    @Inject
    LocationRepository locationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ((TransitApplication) this.getApplicationContext()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_favourite_locations);

        this.favouritePlacesAdapter = new FavouritePlacesAdapter(this, this.favouriteLocationRepository, null);
        RecyclerView recyclerView = this.findViewById(R.id.all_favourite_locations_list);
        recyclerView.setAdapter(this.favouritePlacesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(this.favouritePlacesAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Show the Up button in the action bar.
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<FavouriteLocation> favouriteLocations = this.favouriteLocationRepository.findAll();
        this.favouritePlacesAdapter.setFavouriteLocations(favouriteLocations);

        if (this.deleteAllMenuItem == null) {
            if (favouriteLocations.isEmpty()) {
                this.disableDeleteAllMenuItem = true;
            }
        } else {
            if (favouriteLocations.isEmpty()) {
                this.deleteAllMenuItem.setEnabled(false);
            } else {
                this.deleteAllMenuItem.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.favourite_locations_menu, menu);
        this.deleteAllMenuItem = menu.findItem(R.id.action_remove_all_favourite_locations);

        if (this.disableDeleteAllMenuItem) {
            this.deleteAllMenuItem.setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == R.id.action_remove_all_favourite_locations) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.remove_favourite_locations_dialog_title)
                    .setMessage(R.string.remove_favourite_locations_dialog_message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            FavouriteLocationsActivity.this.favouriteLocationRepository.deleteAll();
                            FavouriteLocationsActivity.this.favouritePlacesAdapter.setFavouriteLocations(new ArrayList<FavouriteLocation>());
                            FavouriteLocationsActivity.this.deleteAllMenuItem.setEnabled(false);
                            View view = FavouriteLocationsActivity.this.findViewById(android.R.id.content);
                            final Snackbar snackbar = Snackbar.make(view, R.string.favourite_locations_removed_message, Snackbar.LENGTH_SHORT);
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
        } else if (item.getItemId() == R.id.action_add_favourite_location) {
            Intent intent = new Intent(this, PlacesActivity.class);
            this.startActivityForResult(intent, ADD_FAVOURITE_LOCATION_CODE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == ADD_FAVOURITE_LOCATION_CODE) {
                Location location = (Location) data.getSerializableExtra(PlacesActivity.LOCATION_KEY);
                if (location != null) {
                    Intent intent = new Intent(this, FavouriteLocationMapActivity.class);
                    intent.putExtra(FAVOURITE_LOCATION_KEY, new FavouriteLocation(null, location));
                    this.startActivityForResult(intent, SHOW_AND_SAVE_FAVOURITE_LOCATION_CODE);
                }
            } else if (requestCode == SHOW_AND_SAVE_FAVOURITE_LOCATION_CODE) {
                FavouriteLocation favouriteLocation = (FavouriteLocation) data.getSerializableExtra(FAVOURITE_LOCATION_KEY);
                if (favouriteLocation != null) {
                    Location location = favouriteLocation.getLocation();
                    Long id = this.locationRepository.save(location);
                    location.setId(id);

                    favouriteLocation.setLocation(location);
                    this.favouriteLocationRepository.save(favouriteLocation);

                    View view = this.findViewById(android.R.id.content);
                    final Snackbar snackbar = Snackbar.make(view, this.getString(R.string.added_favourite_location_snack_bar_text, favouriteLocation.getTitle()), Snackbar.LENGTH_SHORT);
                    snackbar.setAction(R.string.dismiss_snack_bar, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }
            }
        }
    }
}