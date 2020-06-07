package org.mad.transit.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.mad.transit.R;
import org.mad.transit.util.LocationsUtil;

public class NavigationMapFragment extends MapFragment {

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
                    if (LocationsUtil.locationSettingsAvailability(NavigationMapFragment.this.locationManager)) {
                        NavigationMapFragment.this.followMyLocation = true;
                        NavigationMapFragment.this.enableMyLocationAndLocationUpdates();
                    } else {
                        NavigationMapFragment.this.followMyLocation = false;
                        NavigationMapFragment.this.stopLocationUpdates();
                    }
                }
            }
        };

        this.getActivity().registerReceiver(this.locationSettingsChangedReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        if (!LocationsUtil.locationSettingsAvailability(this.locationManager) || !LocationsUtil.locationPermissionsGranted(this.getActivity())) {
            this.zoomOnLocation(this.defaultLocation.latitude, this.defaultLocation.longitude);
        }

        this.enableMyLocation();

        View bottomSheet = this.getActivity().findViewById(R.id.bottom_sheet);
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
    }
}