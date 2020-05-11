package org.mad.transit.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class NavigationMapFragment extends MapFragment {

    private final LatLng defaultLocation = new LatLng(45.254983, 19.844646); //Spomenik Svetozaru Miletiću, Novi Sad

    public static NavigationMapFragment newInstance() {
        return new NavigationMapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.followMyLocation = true;

        this.locationSettingsChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                    if (NavigationMapFragment.this.locationSettingsAvailability()) {
                        NavigationMapFragment.this.followMyLocation = true;
                        NavigationMapFragment.this.enableMyLocationAndLocationUpdates();
                    } else {
                        NavigationMapFragment.this.followMyLocation = false;
                        NavigationMapFragment.this.stopLocationUpdates(true);
                    }
                }
            }
        };

        this.getActivity().registerReceiver(this.locationSettingsChangedReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        if (!this.locationSettingsAvailability() || !this.locationPermissionsGranted()) {
            this.zoomOnLocation(this.defaultLocation.latitude, this.defaultLocation.longitude);
        }
    }
}