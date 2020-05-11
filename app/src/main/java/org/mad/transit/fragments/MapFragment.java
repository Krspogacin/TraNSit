package org.mad.transit.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.mad.transit.R;
import org.mad.transit.activities.SingleStopActivity;
import org.mad.transit.model.Stop;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import lombok.SneakyThrows;

public abstract class MapFragment extends Fragment implements OnMapReadyCallback {

    static final String VIEW_MODEL_ARG = "VIEW_MODEL";
    static final int LOCATION_PERMISSIONS_REQUEST = 1234;
    static final int LOCATION_REQUEST_CHECK_SETTINGS = 4321;
    private static final int INITIAL_ZOOM_VALUE = 16;
    private static final int MIN_ZOOM_VALUE = 14;
    private static final int MAX_ZOOM_VALUE = 18;
    private static final long UPDATE_INTERVAL = 1000;
    private static final long FASTEST_INTERVAL = 500;
    private static final float SMALLEST_DISPLACEMENT = 1f;
    GoogleMap googleMap;
    boolean followMyLocation;
    private boolean locationSettingsNotAvailable;
    private List<Marker> stopMarkers;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationManager locationManager;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    final LatLng defaultLocation = new LatLng(45.254983, 19.844646); //Spomenik Svetozaru Miletiću, Novi Sad
    BroadcastReceiver locationSettingsChangedReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment, container, false);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.stops_map);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (locationSettingsNotAvailable) {
            locationSettingsNotAvailable = false;
        } else {
            enableMyLocationAndLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (locationSettingsChangedReceiver != null) {
            getActivity().unregisterReceiver(locationSettingsChangedReceiver);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);
        this.googleMap.getUiSettings().setCompassEnabled(false);
    }

    void enableMyLocationAndLocationUpdates() {

        enableMyLocation();

        if (followMyLocation) {
            runLocationUpdates();
        }
    }

    /**
     * Put Google watermark and additional views above the initial bottom sheet and slide it along with the bottom sheet
     *
     * @param bottomSheet             Google watermark has to be above this bottom sheet
     * @param bottomSheetHeaderHeight Initial visible bottom sheet height
     * @param viewsToSlide            Zero, one or more views to slide along with the bottom sheet
     */
    void putViewsAboveBottomSheet(View bottomSheet, final int bottomSheetHeaderHeight, final View... viewsToSlide) {

        if (bottomSheet == null || googleMap == null) {
            return;
        }

        googleMap.setPadding(0, 0, 0, bottomSheetHeaderHeight);

        for (View view : viewsToSlide) {
            view.setPadding(0, 0, 0, bottomSheetHeaderHeight);
        }

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                int realBottomSheetOffset = (int) (bottomSheetHeaderHeight + (bottomSheet.getHeight() - bottomSheetHeaderHeight) * slideOffset);
                googleMap.setPadding(0, 0, 0, realBottomSheetOffset);
                for (View view : viewsToSlide) {
                    view.setPadding(0, 0, 0, realBottomSheetOffset);
                }
            }

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }
        });
    }

    void setOnInfoWindowClickListener() {

        if (googleMap == null) {
            return;
        }

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(getContext(), SingleStopActivity.class);
                Stop stop = (Stop) marker.getTag();
                intent.putExtra(SingleStopActivity.STOP_KEY, stop);
                getContext().startActivity(intent);
            }
        });
    }

    void enableMyLocation() {
        if (googleMap != null && locationSettingsAvailability() && locationPermissionsGranted()) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    boolean runLocationUpdates() {
        if (areLocationSettingsAvailable() && areLocationPermissionsGranted()) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location lastLocation = locationResult.getLastLocation();
                    if (lastLocation != null) {
                        zoomOnLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
                    }
                }
            };

            if (locationRequest == null) {
                locationRequest = createLocationRequest();
            }

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            return true;
        } else {
            return false;
        }
    }

    void stopLocationUpdates(boolean disableMyLocation) {
        if (googleMap != null) {

            if (disableMyLocation) {
                googleMap.setMyLocationEnabled(false);
            }

            if (locationCallback != null) {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }
        }
    }

    /**
     * Check if current location is available.
     * If it is, call onLocationSettingsFound method.
     * If not, open location request dialog
     */
    private boolean areLocationSettingsAvailable() {
        if (locationSettingsAvailability()) {
            return true;
        } else {
            retrieveLocationSettings();
            return false;
        }
    }

    boolean locationSettingsAvailability() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void retrieveLocationSettings() {
        if (locationRequest == null) {
            locationRequest = createLocationRequest();
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);

        LocationServices.getSettingsClient(getActivity()).checkLocationSettings(builder.build()).addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {

            @SneakyThrows
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                } catch (ApiException e) {
                    // Location settings are not satisfied. But could be fixed by showing a dialog to the user if status code is RESOLUTION_REQUIRED.
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_CANCELED) {
            locationSettingsNotAvailable = true;
        }
    }

    /**
     * Check if location permissions are granted to our app
     *
     * @return True if permissions are granted, otherwise false
     */
    private boolean areLocationPermissionsGranted() {
        if (locationPermissionsGranted()) {
            return true;
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MapFragment.LOCATION_PERMISSIONS_REQUEST);
            return false;
        }
    }

    boolean locationPermissionsGranted() {
        return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSIONS_REQUEST && locationPermissionsGranted()) {
            enableMyLocationAndLocationUpdates();
        } else {
            locationSettingsNotAvailable = true;
            Toast.makeText(getActivity(), "We are unable to retrieve your current location", Toast.LENGTH_SHORT).show();
        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        return locationRequest;
    }

    public void zoomOnLocation(double latitude, double longitude) {
        if (googleMap == null) {
            return;
        }

        LatLng currentLocationLatLng = new LatLng(latitude, longitude);
        float currentZoom = googleMap.getCameraPosition().zoom;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLocationLatLng)
                .zoom(currentZoom < MIN_ZOOM_VALUE || currentZoom > MAX_ZOOM_VALUE ? INITIAL_ZOOM_VALUE : currentZoom)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void addStopMarker(Stop stop) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .title(stop.getTitle())
                .icon(bitmapDescriptorFromVector())
                .position(new LatLng(stop.getLatitude(), stop.getLongitude())));
        marker.setTag(stop);
        if (stopMarkers == null) {
            stopMarkers = new ArrayList<>();
        }
        stopMarkers.add(marker);
    }

    private BitmapDescriptor bitmapDescriptorFromVector() {
        Drawable vectorDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_bus_marker);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    void registerLocationSettingsChangedReceiver() {
        locationSettingsChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                    locationSettingsNotAvailable = true;
                    if (locationSettingsAvailability()) {
                        enableMyLocation();
                    } else {
                        googleMap.setMyLocationEnabled(false);
                    }
                }
            }
        };

        getActivity().registerReceiver(locationSettingsChangedReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    public List<Marker> getStopMarkers() {
        if (stopMarkers == null) {
            stopMarkers = new ArrayList<>();
        }
        return stopMarkers;
    }

    public void clearStopMarkers() {
        googleMap.clear();
        stopMarkers = new ArrayList<>();
    }

    public void addPolyline(PolylineOptions polylineOptions) {
        googleMap.addPolyline(polylineOptions);
    }
}