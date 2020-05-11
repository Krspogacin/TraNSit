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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.mad.transit.R;
import org.mad.transit.activities.SingleStopActivity;
import org.mad.transit.model.Stop;

import java.util.ArrayList;
import java.util.List;

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
    List<Marker> stopMarkers;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationManager locationManager;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    BroadcastReceiver locationSettingsChangedReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment, container, false);
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.stops_map);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (this.locationSettingsNotAvailable) {
            this.locationSettingsNotAvailable = false;
        } else {
            this.enableMyLocationAndLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.stopLocationUpdates(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);
    }

    void enableMyLocationAndLocationUpdates() {

        this.enableMyLocation();

        if (this.followMyLocation) {
            this.runLocationUpdates();
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

        if (bottomSheet == null || this.googleMap == null) {
            return;
        }

        this.googleMap.setPadding(0, 0, 0, bottomSheetHeaderHeight);

        for (View view : viewsToSlide) {
            view.setPadding(0, 0, 0, bottomSheetHeaderHeight);
        }

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                int realBottomSheetOffset = (int) (bottomSheetHeaderHeight + (bottomSheet.getHeight() - bottomSheetHeaderHeight) * slideOffset);
                MapFragment.this.googleMap.setPadding(0, 0, 0, realBottomSheetOffset);
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

        if (this.googleMap == null) {
            return;
        }

        this.googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MapFragment.this.getContext(), SingleStopActivity.class);
                Stop stop = (Stop) marker.getTag();
                intent.putExtra(SingleStopActivity.STOP_KEY, stop);
                MapFragment.this.getContext().startActivity(intent);
            }
        });
    }

    void enableMyLocation() {
        if (this.googleMap != null && this.locationSettingsAvailability() && this.locationPermissionsGranted()) {
            this.googleMap.setMyLocationEnabled(true);
        }
    }

    boolean runLocationUpdates() {
        if (this.areLocationSettingsAvailable() && this.areLocationPermissionsGranted()) {
            this.locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location lastLocation = locationResult.getLastLocation();
                    if (lastLocation != null) {
                        MapFragment.this.zoomOnLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
                    }
                }
            };

            if (this.locationRequest == null) {
                this.locationRequest = this.createLocationRequest();
            }

            this.fusedLocationProviderClient.requestLocationUpdates(this.locationRequest, this.locationCallback, Looper.myLooper());
            return true;
        } else {
            return false;
        }
    }

    void stopLocationUpdates(boolean disableMyLocation) {
        if (this.googleMap != null) {

            if (disableMyLocation) {
                this.googleMap.setMyLocationEnabled(false);
            }

            if (this.locationCallback != null) {
                this.fusedLocationProviderClient.removeLocationUpdates(this.locationCallback);
            }
        }
    }

    /**
     * Check if current location is available.
     * If it is, call onLocationSettingsFound method.
     * If not, open location request dialog
     */
    private boolean areLocationSettingsAvailable() {
        if (this.locationSettingsAvailability()) {
            return true;
        } else {
            this.retrieveLocationSettings();
            return false;
        }
    }

    boolean locationSettingsAvailability() {
        return this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void retrieveLocationSettings() {
        if (this.locationRequest == null) {
            this.locationRequest = this.createLocationRequest();
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(this.locationRequest)
                .setAlwaysShow(true);

        LocationServices.getSettingsClient(this.getActivity()).checkLocationSettings(builder.build()).addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {

            @SneakyThrows
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                } catch (ApiException e) {
                    // Location settings are not satisfied. But could be fixed by showing a dialog to the user if status code is RESOLUTION_REQUIRED.
                    if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        MapFragment.this.startIntentSenderForResult(resolvable.getResolution().getIntentSender(),
                                MapFragment.LOCATION_REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);
                    } else {
                        Toast.makeText(MapFragment.this.getContext(), "Location settings are not available at the moment", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_CANCELED) {
            this.locationSettingsNotAvailable = true;
        }
    }

    /**
     * Check if location permissions are granted to our app
     *
     * @return True if permissions are granted, otherwise false
     */
    private boolean areLocationPermissionsGranted() {
        if (this.locationPermissionsGranted()) {
            return true;
        } else {
            MapFragment.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MapFragment.LOCATION_PERMISSIONS_REQUEST);
            return false;
        }
    }

    boolean locationPermissionsGranted() {
        return ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSIONS_REQUEST && this.locationPermissionsGranted()) {
            this.enableMyLocationAndLocationUpdates();
        } else {
            this.locationSettingsNotAvailable = true;
            Toast.makeText(this.getActivity(), "We are unable to retrieve your current location", Toast.LENGTH_SHORT).show();
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

    void zoomOnLocation(double latitude, double longitude) {
        if (this.googleMap == null) {
            return;
        }

        LatLng currentLocationLatLng = new LatLng(latitude, longitude);
        float currentZoom = this.googleMap.getCameraPosition().zoom;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLocationLatLng)
                .zoom(currentZoom < MIN_ZOOM_VALUE || currentZoom > MAX_ZOOM_VALUE ? INITIAL_ZOOM_VALUE : currentZoom)
                .build();
        this.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    void addStopMarker(Stop stop) {
        Marker marker = this.googleMap.addMarker(new MarkerOptions()
                .title(stop.getTitle())
                .icon(this.bitmapDescriptorFromVector())
                .position(new LatLng(stop.getLatitude(), stop.getLongitude())));
        marker.setTag(stop);
        if (this.stopMarkers == null) {
            this.stopMarkers = new ArrayList<>();
        }

        this.stopMarkers.add(marker);
    }

    private BitmapDescriptor bitmapDescriptorFromVector() {
        Drawable vectorDrawable = ContextCompat.getDrawable(this.getActivity(), R.drawable.ic_bus_marker);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    void registerLocationSettingsChangedReceiver() {
        this.locationSettingsChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                    MapFragment.this.locationSettingsNotAvailable = true;
                    if (MapFragment.this.locationSettingsAvailability()) {
                        MapFragment.this.enableMyLocation();
                    } else {
                        MapFragment.this.googleMap.setMyLocationEnabled(false);
                    }
                }
            }
        };

        this.getActivity().registerReceiver(this.locationSettingsChangedReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }
}