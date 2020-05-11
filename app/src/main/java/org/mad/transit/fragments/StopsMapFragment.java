package org.mad.transit.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mad.transit.R;
import org.mad.transit.model.NearbyStop;
import org.mad.transit.model.StopsViewModel;

import java.util.List;

public class StopsMapFragment extends MapFragment {

    private StopsViewModel stopsViewModel;
    private View floatingLocationButtonContainer;
    private FloatingActionButton floatingActionButton;
    private final LatLng defaultLocation = new LatLng(45.254983, 19.844646); //Spomenik Svetozaru Miletiću, Novi Sad

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
        //TODO make case savedInstanceState != null to use arguments from previous destroyed fragment instance (https://stackoverflow.com/a/37410735)
        this.stopsViewModel = (StopsViewModel) this.getArguments().getSerializable(MapFragment.VIEW_MODEL_ARG);

        this.registerLocationSettingsChangedReceiver();

        if (this.locationSettingsAvailability() && this.locationPermissionsGranted()) {
            this.followMyLocation = true;
        }
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

        if (!this.locationSettingsAvailability() || !this.locationPermissionsGranted()) {
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

        if (this.googleMap.isMyLocationEnabled()) {
            this.updateFloatingLocationButton(true);
        }
    }

    void updateFloatingLocationButton(boolean followMyLocation) {
        if (followMyLocation) {
            if (this.runLocationUpdates()) {
                this.floatingActionButton.setImageResource(R.drawable.ic_floating_location_on);
            }
        } else {
            this.stopLocationUpdates(false);
            this.floatingActionButton.setImageResource(R.drawable.ic_floating_location_off);
        }
        this.followMyLocation = followMyLocation;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MapFragment.LOCATION_REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK && this.locationPermissionsGranted()) {
            this.floatingActionButton.setImageResource(R.drawable.ic_floating_location_on);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MapFragment.LOCATION_PERMISSIONS_REQUEST && this.locationPermissionsGranted() && this.locationSettingsAvailability()) {
            this.floatingActionButton.setImageResource(R.drawable.ic_floating_location_on);
        }
    }
}
