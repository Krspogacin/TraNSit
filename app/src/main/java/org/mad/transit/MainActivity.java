package org.mad.transit;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.material.tabs.TabLayout;

import org.mad.transit.adapters.NavigationDrawerListAdapter;
import org.mad.transit.adapters.TabAdapter;
import org.mad.transit.model.NavigationItem;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private RelativeLayout drawerPane;
    private ArrayList<NavigationItem> navigationItems = new ArrayList<NavigationItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);

        prepareMenu(navigationItems);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        drawerList = findViewById(R.id.navList);
        drawerPane = findViewById(R.id.drawerPane);

        NavigationDrawerListAdapter adapter = new NavigationDrawerListAdapter(this, navigationItems);
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        drawerList.setAdapter(adapter);

        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                toolbar,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */);

        TabAdapter tabAdapter = new TabAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(tabAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    private void prepareMenu(ArrayList<NavigationItem> navigationItems){
        navigationItems.add(new NavigationItem(getString(R.string.last_routes), R.drawable.last_routes));
        navigationItems.add(new NavigationItem(getString(R.string.favorites), R.drawable.favorites));
        navigationItems.add(new NavigationItem(getString(R.string.home_location), R.drawable.home_location));
        navigationItems.add(new NavigationItem(getString(R.string.work_location), R.drawable.work_location));
        navigationItems.add(new NavigationItem(getString(R.string.settings), R.drawable.settings));
        navigationItems.add(new NavigationItem(getString(R.string.rate_us), R.drawable.rate_us));
        navigationItems.add(new NavigationItem(getString(R.string.about_us), R.drawable.about_us));
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener  {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItemFromDrawer(position);
        }
    }

    private void selectItemFromDrawer(int position) {
        //TODO: change to other activity
        if(position == 0){
            //...
        }else if(position == 1){
            //..
        }else if(position == 2){
            //..
        }else if(position == 3){
            //..
        }else if(position == 4){
            //..
        }else if(position == 5){
            //...
        }else if(position == 6){
            //...
        }else{

        }
        drawerList.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerPane);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawerToggle.onConfigurationChanged(newConfig);
    }
}
