package org.mad.transit.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mad.transit.R;
import org.mad.transit.model.NearbyStop;
import org.mad.transit.model.StopsViewModel;
import org.mad.transit.util.LocationsUtil;

import java.util.List;

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
            this.stopsViewModel = (StopsViewModel) savedInstanceState.getSerializable(MapFragment.VIEW_MODEL_ARG);
        } else {
            this.stopsViewModel = (StopsViewModel) this.getArguments().getSerializable(MapFragment.VIEW_MODEL_ARG);
        }

        if (LocationsUtil.locationSettingsAvailability(this.locationManager) && LocationsUtil.locationPermissionsGranted(this.getActivity())) {
            this.followMyLocation = true;

            if (this.floatingActionButton != null) {
                this.floatingActionButton.setImageResource(R.drawable.ic_my_location_primary_24dp);
            }
        }

        this.locationSettingsChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                    if (LocationsUtil.locationSettingsAvailability(StopsMapFragment.this.locationManager)) {
                        StopsMapFragment.this.enableMyLocation();
                        if (StopsMapFragment.this.followMyLocation) {
                            StopsMapFragment.this.updateFloatingLocationButton(true);
                        }
                    } else {
                        if (StopsMapFragment.this.followMyLocation) {
                            StopsMapFragment.this.updateFloatingLocationButton(false);
                        } else {
                            StopsMapFragment.this.googleMap.setMyLocationEnabled(false);
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

        if (this.stopsViewModel != null) {
            List<NearbyStop> nearbyStops = this.stopsViewModel.getNearbyStopsLiveData().getValue();
            if (nearbyStops != null) {
                for (NearbyStop nearbyStop : nearbyStops) {
                    this.addStopMarker(nearbyStop);
                }
            }
        }

        this.configAboutFloatingLocationButton();

        View bottomSheetHeader = this.getActivity().findViewById(R.id.bottom_sheet_header);
        if (bottomSheetHeader != null) {
            View bottomSheet = this.getActivity().findViewById(R.id.bottom_sheet);
            this.putViewsAboveBottomSheet(bottomSheet, bottomSheetHeader.getHeight(), this.floatingLocationButtonContainer);
        }

        this.setOnInfoWindowClickListener();

        if (!LocationsUtil.locationSettingsAvailability(this.locationManager) || !LocationsUtil.locationPermissionsGranted(this.getActivity())) {
            this.zoomOnLocation(this.defaultLocation.latitude, this.defaultLocation.longitude);
        }
    }

    private void configAboutFloatingLocationButton() {
        this.floatingLocationButtonContainer = this.getActivity().findViewById(R.id.floating_location_button_container);

        this.floatingActionButton = this.getActivity().findViewById(R.id.floating_location_button);

        this.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopsMapFragment.this.updateFloatingLocationButton(true);
            }
        });

        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                StopsMapFragment.this.updateFloatingLocationButton(false);
            }
        });

        this.googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if (i != GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) {
                    StopsMapFragment.this.updateFloatingLocationButton(false);
                }
            }
        });

        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                StopsMapFragment.this.updateFloatingLocationButton(false);
            }
        });

        if (this.followMyLocation) {
            if (!this.googleMap.isMyLocationEnabled()) {
                this.enableMyLocation();
            }
            this.updateFloatingLocationButton(true);
        }
    }

    void updateFloatingLocationButton(boolean followMyLocation) {
        if (followMyLocation) {
            if (this.runLocationUpdates()) {
                this.floatingActionButton.setImageResource(R.drawable.ic_my_location_primary_24dp);
            }
        } else {
            this.stopLocationUpdates(false);
            this.floatingActionButton.setImageResource(R.drawable.ic_my_location_black_24dp);
        }
        this.followMyLocation = followMyLocation;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LocationsUtil.LOCATION_REQUEST_CHECK_SETTINGS &&
                resultCode == Activity.RESULT_OK &&
                LocationsUtil.locationPermissionsGranted(this.getActivity())) {
            this.floatingActionButton.setImageResource(R.drawable.ic_my_location_primary_24dp);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MapFragment.LOCATION_PERMISSIONS_REQUEST
                && LocationsUtil.locationPermissionsGranted(this.getActivity())
                && LocationsUtil.locationSettingsAvailability(this.locationManager)) {
            this.floatingActionButton.setImageResource(R.drawable.ic_my_location_primary_24dp);
        }
    }
}
