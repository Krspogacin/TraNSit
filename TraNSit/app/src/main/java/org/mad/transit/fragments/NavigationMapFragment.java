package org.mad.transit.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mad.transit.R;
import org.mad.transit.activities.NavigationActivity;
import org.mad.transit.dto.ActionDto;
import org.mad.transit.dto.RouteDto;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.util.Constants;
import org.mad.transit.util.LocationsUtil;

import java.util.List;

public class NavigationMapFragment extends MapFragment {

    private RouteDto route;
    private Location startLocation;
    private Location endLocation;
    private FloatingActionButton floatingActionButton;

    public static NavigationMapFragment newInstance() {
        return new NavigationMapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.locationSettingsChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                    if (LocationsUtil.locationSettingsAvailability(NavigationMapFragment.this.locationManager)) {
                        NavigationMapFragment.this.enableMyLocation();
                        if (NavigationMapFragment.this.followMyLocation) {
                            NavigationMapFragment.this.updateFloatingLocationButton(true);
                        }
                    } else {
                        if (NavigationMapFragment.this.followMyLocation) {
                            NavigationMapFragment.this.updateFloatingLocationButton(false);
                        } else {
                            NavigationMapFragment.this.stopLocationUpdates();
                        }
                    }
                }
            }
        };

        this.getActivity().registerReceiver(this.locationSettingsChangedReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        this.configAboutFloatingLocationButton();

        this.enableMyLocation();

        ConstraintLayout indicatorContainer = this.getActivity().findViewById(R.id.indicator_container);
        this.googleMap.setPadding(0, 0, 0, indicatorContainer.getHeight());

        for (ActionDto action : this.route.getActions()) {
            Stop stop = action.getStop();
            if (stop != null) {
                this.addStopMarker(stop);
            }
        }

        for (Pair<Long, LineDirection> key : this.route.getPath().keySet()) {
            List<Location> pathLocations = this.route.getPath().get(key);
            if (pathLocations != null) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(Constants.getLineColor(key.first));

                for (Location pathLocation : pathLocations) {
                    LatLng latLng = new LatLng(pathLocation.getLatitude(), pathLocation.getLongitude());
                    polylineOptions.add(latLng);
                }
                this.addPolyline(polylineOptions);
            }
        }

        this.addLocationMarker(this.startLocation);
        this.addLocationMarker(this.endLocation, this.bitmapDescriptorFromVector(R.drawable.finish_icon));

        ((NavigationActivity) this.getActivity()).zoomOnCurrentPartOfRoute(0);
    }

    private void configAboutFloatingLocationButton() {
        this.floatingActionButton = this.getActivity().findViewById(R.id.navigation_floating_location_button);

        this.floatingActionButton.setOnClickListener(v -> NavigationMapFragment.this.updateFloatingLocationButton(true));

        this.googleMap.setOnMapClickListener(latLng -> NavigationMapFragment.this.updateFloatingLocationButton(false));

        this.googleMap.setOnCameraMoveStartedListener(i -> {
            if (i != GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) {
                NavigationMapFragment.this.updateFloatingLocationButton(false);
            }
        });

        this.googleMap.setOnMapLongClickListener(latLng -> NavigationMapFragment.this.updateFloatingLocationButton(false));
    }

    private void updateFloatingLocationButton(boolean followMyLocation) {
        this.followMyLocation = followMyLocation;
        if (followMyLocation) {
            if (this.runLocationUpdates()) {
                this.floatingActionButton.setImageResource(R.drawable.ic_my_location_primary_24dp);
            }
        } else {
            this.floatingActionButton.setImageResource(R.drawable.ic_my_location_black_24dp);
        }
    }

    public void setRoute(RouteDto route) {
        this.route = route;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public boolean getFollowMyLocation() {
        return this.followMyLocation;
    }

    private void addCircle(Location location) {
        this.googleMap.addCircle(new CircleOptions()
                .center(new LatLng(location.getLatitude(), location.getLongitude()))
                .radius(Constants.GEOFENCE_NOTIFICATION_RADIUS)
                .strokeColor(this.getResources().getColor(R.color.colorPrimary))
                .strokeWidth(2)
                .fillColor(this.getResources().getColor(R.color.colorLightPrimary)));
    }
}