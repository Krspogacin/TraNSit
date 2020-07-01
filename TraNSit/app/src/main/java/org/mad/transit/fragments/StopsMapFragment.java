package org.mad.transit.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.model.NearbyStop;
import org.mad.transit.util.LocationsUtil;
import org.mad.transit.view.model.StopViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class StopsMapFragment extends MapFragment {

    @Inject
    StopViewModel stopViewModel;

    private View floatingLocationButtonContainer;
    private FloatingActionButton floatingActionButton;
    private SharedPreferences defaultSharedPreferences;
    private double stationsRadius;
    private Circle circle;
    private final FrameLayout loadingOverlay;

    public static StopsMapFragment newInstance(FrameLayout loadingOverlay) {
        return new StopsMapFragment(loadingOverlay);
    }

    private StopsMapFragment(FrameLayout loadingOverlay) {
        this.loadingOverlay = loadingOverlay;
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

        if (this.locationCallback == null) {
            this.locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location lastLocation = locationResult.getLastLocation();
                    StopsMapFragment.this.currentLocation = lastLocation;
                    if (lastLocation != null && StopsMapFragment.this.followMyLocation) {
                        StopsMapFragment.this.zoomOnLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
                        StopsMapFragment.this.updateDisplayedNearbyStops();
                    }
                }
            };
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
                            StopsMapFragment.this.stopLocationUpdates();
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

        this.googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                StopsMapFragment.this.updateDisplayedNearbyStops();
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

    public void updateDisplayedNearbyStops() {
        LatLng target = StopsMapFragment.this.googleMap.getCameraPosition().target;

        List<NearbyStop> nearbyStops = new ArrayList<>();
        List<NearbyStop> allNearbyStops = StopsMapFragment.this.stopViewModel.getAllNearbyStops();
        if (allNearbyStops != null) {
            for (NearbyStop nearbyStop : allNearbyStops) {
                double distance = LocationsUtil.calculateDistance(target.latitude, target.longitude, nearbyStop.getLocation().getLatitude(), nearbyStop.getLocation().getLongitude());
                if (distance <= StopsMapFragment.this.stationsRadius) {
                    String walkTime;
                    if (this.currentLocation != null && !this.followMyLocation) {
                        double distanceFromMyLocation = LocationsUtil.calculateDistance(this.currentLocation.getLatitude(), this.currentLocation.getLongitude(), nearbyStop.getLocation().getLatitude(), nearbyStop.getLocation().getLongitude());
                        walkTime = this.calculateWalkTimeForDistance(distanceFromMyLocation);
                    } else {
                        walkTime = this.calculateWalkTimeForDistance(distance);
                    }
                    nearbyStop.setWalkTime(walkTime);
                    nearbyStops.add(nearbyStop);
                }
            }

            if (this.circle != null) {
                this.circle.remove();
            }

            this.circle = this.googleMap.addCircle(new CircleOptions()
                    .center(target)
                    .radius(this.stationsRadius * 1000)
                    .strokeColor(this.getResources().getColor(R.color.colorPrimary))
                    .strokeWidth(2)
                    .fillColor(this.getResources().getColor(R.color.colorLightPrimary)));

            if (this.loadingOverlay.getVisibility() == View.VISIBLE) {
                this.loadingOverlay.setVisibility(View.GONE);
                this.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }

        StopsMapFragment.this.stopViewModel.getNearbyStopsLiveData().setValue(nearbyStops);
    }

    void updateStopMarkers() {
        if (this.googleMap == null) {
            return;
        }

        if (this.googleMap.getCameraPosition().zoom < MapFragment.MIN_ZOOM_VALUE) {
            this.clearMap();
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
                List<NearbyStop> currentStops = new ArrayList<>();
                List<Marker> stopMarkers = new ArrayList<>(this.getStopMarkers());
                this.setStopMarkers(new ArrayList<Marker>());

                //Compare old stops with new stops
                //If old stop has to stay on map (because it is in new stops list), don't remove it from map
                //If old stop is not present in new stops list, then remove it from map
                for (Marker marker : stopMarkers) {
                    NearbyStop stop = (NearbyStop) marker.getTag();

                    if (!nearbyStops.contains(stop)) {
                        marker.remove();
                    } else {
                        this.getStopMarkers().add(marker);
                        currentStops.add(stop);
                    }
                }

                //Add new marker on map for each stop that wasn't already present on map
                for (NearbyStop nearbyStop : nearbyStops) {
                    if (!currentStops.contains(nearbyStop)) {
                        this.addStopMarker(nearbyStop);
                    }
                }
            }
        }
    }

    private String calculateWalkTimeForDistance(double distance) {
        double walkTimeInHours = distance / 4; //taking 4 km/h as average walking speed
        double walkTimeInSeconds = walkTimeInHours * 3600;
        StringBuilder walkTime = new StringBuilder();

        int walkTimeHours = (int) walkTimeInHours;

        if (walkTimeHours > 0) {
            walkTime.append(walkTimeHours);
            int lastDigit = walkTimeHours % 10;
            if (lastDigit == 1 && walkTimeHours != 11) {
                walkTime.append(" sat ");
            } else if ((lastDigit == 2 || lastDigit == 3 || lastDigit == 4) && walkTimeHours != 12 && walkTimeHours != 13 && walkTimeHours != 14) {
                walkTime.append(" sata ");
            } else {
                walkTime.append(" sati ");
            }
        }

        int walkTimeMinutes = (int) ((walkTimeInHours * 60) % 60);

        if (walkTimeMinutes == 1) {
            walkTime.append(walkTimeMinutes).append(" minut ");
        } else if (walkTimeMinutes > 1) {
            walkTime.append(walkTimeMinutes).append(" minuta ");
        }

        int walkTimeSeconds = (int) (walkTimeInSeconds % 60);

        if (walkTimeSeconds > 0) {
            if (walkTimeMinutes > 0) {
                walkTime.append("i ");
            }

            walkTime.append(walkTimeSeconds).append(" ");

            int lastDigit = walkTimeSeconds % 10;
            if (lastDigit == 1 && walkTimeSeconds != 11) {
                walkTime.append("sekunda");
            } else if ((lastDigit == 2 || lastDigit == 3 || lastDigit == 4) && walkTimeSeconds != 12 && walkTimeSeconds != 13 && walkTimeSeconds != 14) {
                walkTime.append("sekunde");
            } else {
                walkTime.append("sekundi");
            }
        } else if (walkTimeMinutes == 0) {
            walkTime.append("/");
        }

        return walkTime.toString();
    }

    void updateFloatingLocationButton(boolean followMyLocation) {
        this.followMyLocation = followMyLocation;
        if (followMyLocation) {
            if (this.runLocationUpdates()) {
                this.floatingActionButton.setImageResource(R.drawable.ic_my_location_primary_24dp);
            }
        } else {
            this.floatingActionButton.setImageResource(R.drawable.ic_my_location_black_24dp);
        }
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
