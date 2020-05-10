package org.mad.transit.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mad.transit.R;
import org.mad.transit.activities.SingleStopActivity;
import org.mad.transit.model.NearbyStop;
import org.mad.transit.model.Stop;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import lombok.SneakyThrows;

public abstract class MapFragment extends Fragment implements OnMapReadyCallback {

    static final String VIEW_MODEL_ARG = "VIEW_MODEL";
    private static final int LOCATION_PERMISSIONS_REQUEST = 1234;
    private static final int LOCATION_REQUEST_CHECK_SETTINGS = 4321;
    private static final int INITIAL_ZOOM_VALUE = 16;
    private static final int MIN_ZOOM_VALUE = 14;
    private static final int MAX_ZOOM_VALUE = 18;
    private static final long UPDATE_INTERVAL = 1000;
    private static final long FASTEST_INTERVAL = 500;
    private static final float SMALLEST_DISPLACEMENT = 1f;
    private GoogleMap googleMap;
    private int bottomSheetHeaderHeight;
    private LocationManager locationManager;
    private View floatingLocationButtonContainer;
    private boolean followLocation;
    private FloatingActionButton floatingActionButton;
    private boolean locationTurnedOn;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private final LatLng defaultLocation = new LatLng(45.254983, 19.844646); //Spomenik Svetozaru Miletiću, Novi Sad
    private List<Marker> stopMarkers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stops_map, container, false);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.stops_map);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (googleMap != null &&
                (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            googleMap.setMyLocationEnabled(true);

            if (followLocation) {
                runLocationUpdates();
            }
        }

        if (locationTurnedOn) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MapFragment.LOCATION_PERMISSIONS_REQUEST);
            } else {
                retrieveAndZoomOnCurrentLocation();
            }
            locationTurnedOn = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (googleMap != null) {
            googleMap.setMyLocationEnabled(false);
            if (locationCallback != null) {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSIONS_REQUEST &&
                (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            retrieveAndZoomOnCurrentLocation();
            updateFloatingLocationButton(true);
        } else {
            zoomOnLocation(defaultLocation.latitude, defaultLocation.longitude);
            Toast.makeText(getActivity(), "We are unable to retrieve your current location", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);

        //Put Google watermark above the initial bottom sheet
        bottomSheetHeaderHeight = getActivity().findViewById(R.id.bottom_sheet_header).getHeight();
        this.googleMap.setPadding(0, 0, 0, bottomSheetHeaderHeight);

        View bottomSheet = getActivity().findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //Slide Google watermark along with the bottom sheet
                int realBottomSheetOffset = (int) (bottomSheetHeaderHeight + (bottomSheet.getHeight() - bottomSheetHeaderHeight) * slideOffset);
                MapFragment.this.googleMap.setPadding(0, 0, 0, realBottomSheetOffset);
                if (floatingLocationButtonContainer != null) {
                    floatingLocationButtonContainer.setPadding(0, 0, 0, realBottomSheetOffset);
                }
            }

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }
        });

        this.googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(getContext(), SingleStopActivity.class);
                NearbyStop nearbyStop = (NearbyStop) marker.getTag();
                intent.putExtra(SingleStopActivity.NEARBY_STOP_KEY, nearbyStop);
                getContext().startActivity(intent);
            }
        });

        //If there is no GPS and/or NETWORK provider, skip permissions check
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            zoomOnLocation(defaultLocation.latitude, defaultLocation.longitude);
            return;
        }

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            retrieveAndZoomOnCurrentLocation();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSIONS_REQUEST);
        }
    }

    void configAboutFloatingLocationButton() {
        floatingLocationButtonContainer = getActivity().findViewById(R.id.floating_location_button_container);
        floatingLocationButtonContainer.setPadding(0, 0, 0, bottomSheetHeaderHeight);

        floatingActionButton = getActivity().findViewById(R.id.floating_location_button);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If there is no GPS and/or NETWORK provider, skip permissions check
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                        !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    checkIfLocationIsAvailable();
                    return;
                }

                // Check for location permissions
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {

                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                zoomOnLocation(location.getLatitude(), location.getLongitude());
                                updateFloatingLocationButton(true);
                            }
                        }
                    });
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            MapFragment.LOCATION_PERMISSIONS_REQUEST);
                }
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
    }

    void updateFloatingLocationButton(boolean followLocation) {
        if (floatingActionButton != null) {
            if (followLocation) {
                floatingActionButton.setImageResource(R.drawable.ic_floating_location_on);
                runLocationUpdates();
            } else {
                floatingActionButton.setImageResource(R.drawable.ic_floating_location_off);
                if (locationCallback != null) {
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                }
            }
        }
        MapFragment.this.followLocation = followLocation;
    }

    private void runLocationUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location lastLocation = locationResult.getLastLocation();
                if (lastLocation != null) {
                    zoomOnLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void retrieveAndZoomOnCurrentLocation() {
        if (googleMap == null ||
                (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            return;
        }

        googleMap.setMyLocationEnabled(true);
        checkIfLocationIsAvailable();
    }

    /**
     * Check if current location is available.
     * If it is, enable location following with camera and zoom on it.
     * If not, open location request dialog.
     */
    private void checkIfLocationIsAvailable() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);
        LocationServices.getSettingsClient(getActivity()).checkLocationSettings(builder.build()).addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {

            @SneakyThrows
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        updateFloatingLocationButton(true);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                MapFragment.LOCATION_PERMISSIONS_REQUEST);
                    }
                } catch (ApiException e) {
                    // Location settings are not satisfied. But could be fixed by showing a dialog to the user.
                    if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        startIntentSenderForResult(resolvable.getResolution().getIntentSender(),
                                MapFragment.LOCATION_REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);
                    } else {
                        Toast.makeText(getContext(), "Location settings are not available at the moment", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void zoomOnLocation(double latitude, double longitude) {
        LatLng currentLocationLatLng = new LatLng(latitude, longitude);
        float currentZoom = googleMap.getCameraPosition().zoom;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLocationLatLng)
                .zoom(currentZoom < MIN_ZOOM_VALUE || currentZoom > MAX_ZOOM_VALUE ? INITIAL_ZOOM_VALUE : currentZoom)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
            locationTurnedOn = true;
        }
    }

    public List<Marker> getStopMarkers() {
        return stopMarkers;
    }

    public void addStopMarker(Stop stop) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .title(stop.getName())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_icon))
                .position(new LatLng(stop.getLatitude(), stop.getLongitude())));
        marker.setTag(stop);
        if (stopMarkers == null) {
            stopMarkers = new ArrayList<>();
        }
        stopMarkers.add(marker);
    }
}