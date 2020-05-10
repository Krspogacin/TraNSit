package org.mad.transit.fragments;

import com.google.android.gms.maps.GoogleMap;

public class NavigationMapFragment extends MapFragment {

    public static NavigationMapFragment newInstance() {
        return new NavigationMapFragment();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
    }
}