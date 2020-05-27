package org.mad.transit;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import org.mad.transit.activities.FavouriteLinesActivity;
import org.mad.transit.activities.SettingsActivity;
import org.mad.transit.adapters.NavigationDrawerListAdapter;
import org.mad.transit.adapters.TabAdapter;
import org.mad.transit.model.NavigationItem;
import org.mad.transit.sync.InitializeDatabaseTask;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import lombok.SneakyThrows;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private RelativeLayout drawerPane;
    private final ArrayList<NavigationItem> navigationItems = new ArrayList<NavigationItem>();

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        //INIT DATABASE
        new InitializeDatabaseTask(this).execute();
    }

    private void prepareMenu(ArrayList<NavigationItem> navigationItems) {
        navigationItems.add(new NavigationItem(this.getString(R.string.last_routes), R.drawable.ic_send_black_24dp));
        navigationItems.add(new NavigationItem(this.getString(R.string.favorites), R.drawable.ic_star_black_24dp));
        navigationItems.add(new NavigationItem(this.getString(R.string.set_location), R.drawable.ic_my_location_black_24dp));
        //navigationItems.add(new NavigationItem(getString(R.string.home_location), R.drawable.ic_home_black_24dp));
        //navigationItems.add(new NavigationItem(getString(R.string.work_location), R.drawable.ic_business_black_24dp));
        navigationItems.add(new NavigationItem(this.getString(R.string.settings), R.drawable.ic_settings_black_24dp));
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MainActivity.this.selectItemFromDrawer(position);
        }
    }

    private void selectItemFromDrawer(int position) {
        if (position == 0) {
            Toast.makeText(this, "Poslednje rute", Toast.LENGTH_SHORT).show();
        } else if (position == 1) {
            Intent intent = new Intent(this, FavouriteLinesActivity.class);
            this.startActivity(intent);
        } else if (position == 2) {
            Toast.makeText(this, "Zapamti lokaciju", Toast.LENGTH_SHORT).show();
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        this.drawerToggle.onConfigurationChanged(newConfig);
    }
}
