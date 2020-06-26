package org.mad.transit.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.mad.transit.R;
import org.mad.transit.model.Location;
import org.mad.transit.navigation.GeofenceHelper;
import org.mad.transit.util.Constants;
import org.mad.transit.util.LocationsUtil;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NavigationMapFragment extends MapFragment {
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;

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
        geofencingClient = LocationServices.getGeofencingClient(this.getActivity());
        geofenceHelper = new GeofenceHelper(this.getActivity());
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 0);
            }
        }
    }

     private void addCircle(Location location){
         this.googleMap.addCircle(new CircleOptions()
                 .center(new LatLng(location.getLatitude(), location.getLongitude()))
                 .radius(Constants.GEOFENCE_NOTIFICATION_RADIUS)
                 .strokeColor(this.getResources().getColor(R.color.colorPrimary))
                 .strokeWidth(2)
                 .fillColor(this.getResources().getColor(R.color.colorLightPrimary)));
     }
}