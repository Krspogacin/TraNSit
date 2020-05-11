package org.mad.transit.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mad.transit.R;
import org.mad.transit.model.NearbyStop;
import org.mad.transit.model.StopsViewModel;

import java.util.List;

import androidx.annotation.NonNull;

public class StopsMapFragment extends MapFragment {

    private StopsViewModel stopsViewModel;
    private View floatingLocationButtonContainer;
    private FloatingActionButton floatingActionButton;

    public static StopsMapFragment newInstance(StopsViewModel stopsViewModel) {
        StopsMapFragment stopsMapFragment = new StopsMapFragment();

        Bundle args = new Bundle();
        args.putSerializable(MapFragment.VIEW_MODEL_ARG, stopsViewModel);
        stopsMapFragment.setArguments(args);

        return stopsMapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            stopsViewModel = (StopsViewModel) savedInstanceState.getSerializable(MapFragment.VIEW_MODEL_ARG);
        } else {
            stopsViewModel = (StopsViewModel) getArguments().getSerializable(MapFragment.VIEW_MODEL_ARG);
        }

        if (locationSettingsAvailability() && locationPermissionsGranted()) {
            followMyLocation = true;

            if (floatingActionButton != null) {
                floatingActionButton.setImageResource(R.drawable.ic_floating_location_on);
            }
        }

        locationSettingsChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                    if (locationSettingsAvailability()) {
                        enableMyLocation();
                        if (followMyLocation) {
                            updateFloatingLocationButton(true);
                        }
                    } else {
                        if (followMyLocation) {
                            updateFloatingLocationButton(false);
                        } else {
                            googleMap.setMyLocationEnabled(false);
                        }
                    }
                }
            }
        };

        getActivity().registerReceiver(locationSettingsChangedReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        if (stopsViewModel != null) {
            List<NearbyStop> nearbyStops = stopsViewModel.getNearbyStopsLiveData().getValue();
            if (nearbyStops != null) {
                for (NearbyStop nearbyStop : nearbyStops) {
                    addStopMarker(nearbyStop);
                }
            }
        }

        configAboutFloatingLocationButton();

        View bottomSheetHeader = getActivity().findViewById(R.id.bottom_sheet_header);
        if (bottomSheetHeader != null) {
            View bottomSheet = getActivity().findViewById(R.id.bottom_sheet);
            putViewsAboveBottomSheet(bottomSheet, bottomSheetHeader.getHeight(), floatingLocationButtonContainer);
        }

        setOnInfoWindowClickListener();

        if (!locationSettingsAvailability() || !locationPermissionsGranted()) {
            zoomOnLocation(defaultLocation.latitude, defaultLocation.longitude);
        }
    }

    private void configAboutFloatingLocationButton() {
        floatingLocationButtonContainer = getActivity().findViewById(R.id.floating_location_button_container);

        floatingActionButton = getActivity().findViewById(R.id.floating_location_button);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFloatingLocationButton(true);
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                updateFloatingLocationButton(false);
            }
        });

        googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if (i != GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) {
                    updateFloatingLocationButton(false);
                }
            }
        });

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                updateFloatingLocationButton(false);
            }
        });

        if (!googleMap.isMyLocationEnabled()) {
            enableMyLocation();
        }
        updateFloatingLocationButton(true);
    }

    void updateFloatingLocationButton(boolean followMyLocation) {
        if (followMyLocation) {
            if (runLocationUpdates()) {
                floatingActionButton.setImageResource(R.drawable.ic_floating_location_on);
            }
        } else {
            stopLocationUpdates(false);
            floatingActionButton.setImageResource(R.drawable.ic_floating_location_off);
        }
        this.followMyLocation = followMyLocation;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MapFragment.LOCATION_REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK && locationPermissionsGranted()) {
            floatingActionButton.setImageResource(R.drawable.ic_floating_location_on);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MapFragment.LOCATION_PERMISSIONS_REQUEST && locationPermissionsGranted() && locationSettingsAvailability()) {
            floatingActionButton.setImageResource(R.drawable.ic_floating_location_on);
        }
    }
}
