package org.mad.transit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;
import org.mad.transit.activities.FavouriteLinesActivity;
import org.mad.transit.activities.FavouriteLocationsActivity;
import org.mad.transit.activities.PastDirectionsActivity;
import org.mad.transit.activities.SettingsActivity;
import org.mad.transit.adapters.NavigationDrawerListAdapter;
import org.mad.transit.adapters.TabAdapter;
import org.mad.transit.model.NavigationItem;
import org.mad.transit.repository.DepartureTimeRepository;
import org.mad.transit.repository.LineRepository;
import org.mad.transit.repository.TimetableRepository;
import org.mad.transit.task.RetrieveTimetablesAsyncTask;
import org.mad.transit.util.Constants;

import java.util.ArrayList;
import java.util.Calendar;

import javax.inject.Inject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import lombok.SneakyThrows;

public class MainActivity extends AppCompatActivity {

    private static final String MONTH = "month";
    private static final String YEAR = "year";
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private RelativeLayout drawerPane;
    private final ArrayList<NavigationItem> navigationItems = new ArrayList<>();

    @Inject
    TimetableRepository timetableRepository;

    @Inject
    DepartureTimeRepository departureTimeRepository;

    @Inject
    LineRepository lineRepository;

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ((TransitApplication) this.getApplicationContext()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.navigation_drawer);

        this.prepareMenu(this.navigationItems);
        Toolbar toolbar = this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        this.drawerLayout = this.findViewById(R.id.drawerLayout);
        this.drawerList = this.findViewById(R.id.navList);
        this.drawerPane = this.findViewById(R.id.drawerPane);

        NavigationDrawerListAdapter adapter = new NavigationDrawerListAdapter(this, this.navigationItems);
        this.drawerList.setOnItemClickListener(new DrawerItemClickListener());
        this.drawerList.setAdapter(adapter);

        this.drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                this.drawerLayout,         /* DrawerLayout object */
                toolbar,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */);

        TabAdapter tabAdapter = new TabAdapter(this, this.getSupportFragmentManager());
        ViewPager viewPager = this.findViewById(R.id.view_pager);
        viewPager.setAdapter(tabAdapter);
        TabLayout tabs = this.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        Calendar calendar = Calendar.getInstance();
        final int currentMonth = calendar.get(Calendar.MONTH);
        final int currentYear = calendar.get(Calendar.YEAR);

        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final int month = defaultSharedPreferences.getInt(MONTH, 5);
        final int year = defaultSharedPreferences.getInt(YEAR, 2020);

        if (month != currentMonth || year != currentYear) {
            boolean autoSyncEnabled = defaultSharedPreferences.getBoolean(this.getString(R.string.sync_preference_pref_key), false);
            if (!autoSyncEnabled) {
                TextView syncIsAvailableMessageTextView = this.findViewById(R.id.sync_is_available_message_text_view);
                syncIsAvailableMessageTextView.setText(this.getString(R.string.sync_is_available_message, Constants.getMonth(currentMonth)));

                final LinearLayout syncIsAvailableContainer = this.findViewById(R.id.sync_is_available_container);
                syncIsAvailableContainer.setVisibility(View.VISIBLE);

                Button doSyncButton = this.findViewById(R.id.do_sync);
                doSyncButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final FrameLayout loadingOverlay = MainActivity.this.findViewById(R.id.loading_overlay);
                        loadingOverlay.setVisibility(View.VISIBLE);
                        MainActivity.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        RetrieveTimetablesAsyncTask retrieveTimetablesAsyncTask = new RetrieveTimetablesAsyncTask(MainActivity.this.getContentResolver(),
                                MainActivity.this.timetableRepository,
                                MainActivity.this.lineRepository,
                                MainActivity.this.departureTimeRepository,
                                new RetrieveTimetablesAsyncTask.TaskListener() {
                                    @Override
                                    public void onFinished() {
                                        defaultSharedPreferences.edit().putInt(MainActivity.MONTH, currentMonth).apply();
                                        defaultSharedPreferences.edit().putInt(MainActivity.YEAR, currentYear).apply();

                                        loadingOverlay.setVisibility(View.GONE);
                                        syncIsAvailableContainer.setVisibility(View.GONE);
                                        MainActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                        View view = MainActivity.this.findViewById(android.R.id.content);
                                        final Snackbar snackbar = Snackbar.make(view, R.string.successful_sync_message, Snackbar.LENGTH_SHORT);
                                        snackbar.setAction(R.string.dismiss_snack_bar, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                snackbar.dismiss();
                                            }
                                        });
                                        snackbar.show();
                                    }
                                });

                        retrieveTimetablesAsyncTask.execute();
                    }
                });

                Button closeSyncIsAvailableMessage = this.findViewById(R.id.close_sync_is_available_message);
                closeSyncIsAvailableMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        syncIsAvailableContainer.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    private void prepareMenu(ArrayList<NavigationItem> navigationItems) {
        navigationItems.add(new NavigationItem(this.getString(R.string.last_routes), R.drawable.ic_history_black_24dp));
        navigationItems.add(new NavigationItem(this.getString(R.string.favorite_lines), R.drawable.ic_star_black_24dp));
        navigationItems.add(new NavigationItem(this.getString(R.string.favourite_locations), R.drawable.ic_favourite_marker_black));
        navigationItems.add(new NavigationItem(this.getString(R.string.settings), R.drawable.ic_settings_black_24dp));
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MainActivity.this.selectItemFromDrawer(position);
        }
    }

    private void selectItemFromDrawer(int position) {
        if (position == 0) {
            Intent intent = new Intent(this, PastDirectionsActivity.class);
            this.startActivity(intent);
        } else if (position == 1) {
            Intent intent = new Intent(this, FavouriteLinesActivity.class);
            this.startActivity(intent);
        } else if (position == 2) {
            Intent intent = new Intent(this, FavouriteLocationsActivity.class);
            this.startActivity(intent);
        } else if (position == 3) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            this.startActivity(intent);
        }
        this.drawerList.setItemChecked(position, true);
        this.drawerLayout.closeDrawer(this.drawerPane);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        this.drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        this.drawerToggle.onConfigurationChanged(newConfig);
    }
}
