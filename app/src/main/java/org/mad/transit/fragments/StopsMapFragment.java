package org.mad.transit.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mad.transit.R;
import org.mad.transit.model.NearbyStop;
import org.mad.transit.model.StopsFragmentViewModel;

import java.util.Arrays;

import lombok.SneakyThrows;

public class StopsMapFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private static final int LOCATION_PERMISSIONS_REQUEST = 1234;
    private static final int LOCATION_REQUEST_CHECK_SETTINGS = 4321;
    private static final int INITIAL_ZOOM_VALUE = 16;
    private static final int MIN_ZOOM_VALUE = 14;
    private static StopsFragmentViewModel stopsFragmentViewModel;
    private GoogleMap googleMap;
    private int bottomSheetHeaderHeight;
    private LocationManager locationManager;
    private View floatingLocationButtonContainer;
    private boolean followLocation;
    private FloatingActionButton floatingActionButton;
    private boolean locationTurnedOn;
    private FusedLocationProviderClient fusedLocationProviderClient;
    ;

    static StopsMapFragment newInstance(StopsFragmentViewModel stopsFragmentViewModel) {
        StopsMapFragment.stopsFragmentViewModel = stopsFragmentViewModel;
        return new StopsMapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stops_map, container, false);
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.stops_map);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (this.googleMap != null &&
                (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            this.googleMap.setMyLocationEnabled(true);
        }

        if (this.locationTurnedOn) {
            if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                StopsMapFragment.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        StopsMapFragment.LOCATION_PERMISSIONS_REQUEST);
            } else {
                this.retrieveAndZoomOnCurrentLocation();
            }
            this.locationTurnedOn = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (this.googleMap != null) {
            this.googleMap.setMyLocationEnabled(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSIONS_REQUEST &&
                (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            this.retrieveAndZoomOnCurrentLocation();
            StopsMapFragment.this.updateFloatingLocationButton(true);
        } else {
            Toast.makeText(this.getActivity(), "We are unable to retrieve your current location", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        Toast.makeText(this.getContext(), "CAO MAPOOO", Toast.LENGTH_SHORT).show();

        //Put Google watermark above the initial bottom sheet
        this.bottomSheetHeaderHeight = this.getActivity().findViewById(R.id.stops_bottom_sheet_header).getHeight();
        this.googleMap.setPadding(0, 0, 0, this.bottomSheetHeaderHeight);

        this.configAboutFloatingLocationButton();

        View bottomSheet = this.getActivity().findViewById(R.id.stops_bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //Slide Google watermark along with the bottom sheet
                int realBottomSheetOffset = (int) (StopsMapFragment.this.bottomSheetHeaderHeight + (bottomSheet.getHeight() - StopsMapFragment.this.bottomSheetHeaderHeight) * slideOffset);
                StopsMapFragment.this.googleMap.setPadding(0, 0, 0, realBottomSheetOffset);
                StopsMapFragment.this.floatingLocationButtonContainer.setPadding(0, 0, 0, realBottomSheetOffset);
            }

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }
        });

        // Draw bus markers for each nearby stop
        if (stopsFragmentViewModel.getNearbyStopsLiveData().getValue() != null) {
            for (NearbyStop nearbyStop : stopsFragmentViewModel.getNearbyStopsLiveData().getValue()) {
                this.googleMap.addMarker(new MarkerOptions()
                        .title(nearbyStop.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_icon))
                        .position(new LatLng(nearbyStop.getLatitude(), nearbyStop.getLongitude())));
            }
        }

        //If there is no GPS and/or NETWORK provider, skip permissions check
        if (!this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return;
        }

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.retrieveAndZoomOnCurrentLocation();
        } else {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSIONS_REQUEST);
        }
    }

    private void configAboutFloatingLocationButton() {
        this.floatingLocationButtonContainer = this.getActivity().findViewById(R.id.floating_location_button_container);
        this.floatingLocationButtonContainer.setPadding(0, 0, 0, this.bottomSheetHeaderHeight);

        this.floatingActionButton = this.getActivity().findViewById(R.id.floating_location_button);

        this.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If there is no GPS and/or NETWORK provider, skip permissions check
                if (!StopsMapFragment.this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                        !StopsMapFragment.this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    StopsMapFragment.this.retrieveLocation();
                    return;
                }

                // Check for location permissions
                if (ActivityCompat.checkSelfPermission(StopsMapFragment.this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(StopsMapFragment.this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    StopsMapFragment.this.fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {

                        @Override
                        public void onSuccess(Location location) {
                            StopsMapFragment.this.zoomOnCurrentLocation(location);

                            if (location != null) {
                                StopsMapFragment.this.updateFloatingLocationButton(true);
                            }
                        }
                    });
                } else {
                    StopsMapFragment.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            StopsMapFragment.LOCATION_PERMISSIONS_REQUEST);
                }
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
    }

    private void updateFloatingLocationButton(boolean followLocation) {
        if (followLocation) {
            this.floatingActionButton.setImageResource(R.drawable.ic_floating_location_on);
        } else {
            this.floatingActionButton.setImageResource(R.drawable.ic_floating_location_off);
        }
        StopsMapFragment.this.followLocation = followLocation;
    }

    private void retrieveAndZoomOnCurrentLocation() {
        if (this.googleMap == null ||
                (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            return;
        }

        this.googleMap.setMyLocationEnabled(true);

        //Retrieve current location and zoom on it
        this.retrieveLocation();
    }

    private void retrieveLocation() {
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        final LocationRequest locationRequestHighAccuracy = LocationRequest.create();
        locationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addAllLocationRequests(Arrays.asList(locationRequest, locationRequestHighAccuracy))
                .setAlwaysShow(true);
        LocationServices.getSettingsClient(this.getActivity()).checkLocationSettings(builder.build()).addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {

            @SneakyThrows
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                    if (ActivityCompat.checkSelfPermission(StopsMapFragment.this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(StopsMapFragment.this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        StopsMapFragment.this.fusedLocationProviderClient.requestLocationUpdates(locationRequestHighAccuracy, new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                StopsMapFragment.this.zoomOnCurrentLocation(locationResult.getLastLocation());

                                if (locationResult.getLastLocation() != null) {
                                    StopsMapFragment.this.updateFloatingLocationButton(true);
                                    StopsMapFragment.this.fusedLocationProviderClient.removeLocationUpdates(this);
                                }
                            }
                        }, Looper.myLooper());
                    } else {
                        StopsMapFragment.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                StopsMapFragment.LOCATION_PERMISSIONS_REQUEST);
                    }
                } catch (ApiException e) {
                    // Location settings are not satisfied. But could be fixed by showing a dialog to the user.
                    if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        StopsMapFragment.this.startIntentSenderForResult(resolvable.getResolution().getIntentSender(),
                                StopsMapFragment.LOCATION_REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);
                    } else {
                        Toast.makeText(StopsMapFragment.this.getContext(), "Location settings are not available at the moment", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
            this.locationTurnedOn = true;
        }
    }

    private void zoomOnCurrentLocation(Location currentLocation) {
        if (currentLocation == null) {
            Toast.makeText(this.getContext(), "No location :(", Toast.LENGTH_SHORT).show();
            return;
        }
        LatLng currentLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLocationLatLng)
                .zoom(this.googleMap.getCameraPosition().zoom < MIN_ZOOM_VALUE ? INITIAL_ZOOM_VALUE : this.googleMap.getCameraPosition().zoom)
                .build();
        StopsMapFragment.this.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onLocationChanged(Location location) {
        // Move the camera along with the device's location if this kind of an option is enabled
        if (this.followLocation) {
            StopsMapFragment.this.zoomOnCurrentLocation(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}