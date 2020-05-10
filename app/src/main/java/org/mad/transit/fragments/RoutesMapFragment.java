package org.mad.transit.fragments;

import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;

import org.mad.transit.model.RoutesViewModel;

public class RoutesMapFragment extends MapFragment {

    private RoutesViewModel routesViewModel;

    public static RoutesMapFragment newInstance(RoutesViewModel routesViewModel) {
        RoutesMapFragment routesMapFragment = new RoutesMapFragment();

        Bundle args = new Bundle();
        args.putSerializable(VIEW_MODEL_ARG, routesViewModel);
        routesMapFragment.setArguments(args);

        return routesMapFragment;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        configAboutFloatingLocationButton();
    }
}
