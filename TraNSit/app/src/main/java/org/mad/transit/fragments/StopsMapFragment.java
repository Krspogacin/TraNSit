package org.mad.transit.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.activities.SingleStopActivity;
import org.mad.transit.model.NearbyStop;
import org.mad.transit.model.Stop;
import org.mad.transit.repository.LineRepository;
import org.mad.transit.util.LocationsUtil;
import org.mad.transit.view.model.StopViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class StopsMapFragment extends MapFragment {

    @Inject
    StopViewModel stopViewModel;

    @Inject
    LineRepository lineRepository;

    private View floatingLocationButtonContainer;
    private FloatingActionButton floatingActionButton;
    private SharedPreferences defaultSharedPreferences;
    private double stationsRadius;
    private boolean markerClicked;
    boolean bottomSheetItemClicked;

    public static StopsMapFragment newInstance() {
        return new StopsMapFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {

        ((TransitApplication) this.getActivity().getApplicationContext()).getAppComponent().inject(this);

        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        this.stationsRadius = Double.parseDouble(this.defaultSharedPreferences.getString(this.getString(R.string.stations_radius_pref_key), "1"));

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
    public void onResume() {
        super.onResume();
        double stationsRadius = Double.parseDouble(this.defaultSharedPreferences.getString(this.getString(R.string.stations_radius_pref_key), "1"));
        if (this.stationsRadius != stationsRadius) {
            this.stationsRadius = stationsRadius;
            this.updateDisplayedNearbyStops();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        this.configAboutFloatingLocationButton();

        View bottomSheetHeader = this.getActivity().findViewById(R.id.bottom_sheet_header);
        if (bottomSheetHeader != null) {
            View bottomSheet = this.getActivity().findViewById(R.id.bottom_sheet);
            this.putViewsAboveBottomSheet(bottomSheet, bottomSheetHeader.getHeight(), this.floatingLocationButtonContainer);
        }

        this.googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(StopsMapFragment.this.getContext(), SingleStopActivity.class);
                Stop stop = (Stop) marker.getTag();

                if (stop == null) {
                    return;
                }

                //Retrieve all lines available at this stop
                if (stop.getLines() == null) {
                    stop.setLines(StopsMapFragment.this.lineRepository.findAllByStopId(stop.getId()));
                }

                intent.putExtra(SingleStopActivity.STOP_KEY, stop);
                StopsMapFragment.this.getContext().startActivity(intent);
            }
        });

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

        this.googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (StopsMapFragment.this.markerClicked) {
                    StopsMapFragment.this.markerClicked = false;
                } else if (StopsMapFragment.this.bottomSheetItemClicked) {
                    StopsMapFragment.this.bottomSheetItemClicked = false;
                } else {
                    StopsMapFragment.this.updateDisplayedNearbyStops();
                }
            }
        });

        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                StopsMapFragment.this.updateFloatingLocationButton(false);
            }
        });

        this.googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                StopsMapFragment.this.markerClicked = true;
                return false;
            }
        });

        if (this.followMyLocation) {
            if (!this.googleMap.isMyLocationEnabled()) {
                this.enableMyLocation();
            }
            this.updateFloatingLocationButton(true);
        }
    }

    public void updateDisplayedNearbyStops() {
        LatLng target = StopsMapFragment.this.googleMap.getCameraPosition().target;

        List<NearbyStop> nearbyStops = new ArrayList<>();
        List<NearbyStop> allNearbyStops = StopsMapFragment.this.stopViewModel.getAllNearbyStops();
        if (allNearbyStops != null) {
            for (NearbyStop nearbyStop : allNearbyStops) {
                double distance = LocationsUtil.calculateDistance(target.latitude, target.longitude, nearbyStop.getLocation().getLatitude(), nearbyStop.getLocation().getLongitude());
                if (distance <= StopsMapFragment.this.stationsRadius) {
                    nearbyStops.add(nearbyStop);
                }
            }
        }

        StopsMapFragment.this.stopViewModel.getNearbyStopsLiveData().setValue(nearbyStops);
    }

    void updateStopMarkers() {
        if (this.googleMap == null) {
            return;
        }

        this.clearMap();

        if (this.googleMap.getCameraPosition().zoom < MapFragment.MIN_ZOOM_VALUE) {
            return;
        }

        List<NearbyStop> nearbyStops = this.stopViewModel.getNearbyStopsLiveData().getValue();

        if (nearbyStops != null) {
            List<NearbyStop> allNearbyStops = StopsMapFragment.this.stopViewModel.getAllNearbyStops();

            if (allNearbyStops == null) {
                return;
            }

            if (nearbyStops.size() == allNearbyStops.size()) {
                this.updateDisplayedNearbyStops();
            } else {
                for (NearbyStop nearbyStop : nearbyStops) {
                    this.addStopMarker(nearbyStop);
                }
            }
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
