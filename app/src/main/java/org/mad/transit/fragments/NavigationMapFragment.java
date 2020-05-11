package org.mad.transit.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.mad.transit.R;

import androidx.annotation.Nullable;

public class NavigationMapFragment extends MapFragment {

    public static NavigationMapFragment newInstance() {
        return new NavigationMapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        followMyLocation = true;

        locationSettingsChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                    if (locationSettingsAvailability()) {
                        followMyLocation = true;
                        enableMyLocationAndLocationUpdates();
                    } else {
                        followMyLocation = false;
                        stopLocationUpdates(true);
                    }
                }
            }
        };

        getActivity().registerReceiver(locationSettingsChangedReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        if (!locationSettingsAvailability() || !locationPermissionsGranted()) {
            zoomOnLocation(defaultLocation.latitude, defaultLocation.longitude);
        }

        enableMyLocation();

        View bottomSheet = getActivity().findViewById(R.id.bottom_sheet);
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
    }
}