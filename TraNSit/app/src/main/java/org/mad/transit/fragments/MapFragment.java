package org.mad.transit.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import org.mad.transit.R;
import org.mad.transit.activities.SingleStopActivity;
import org.mad.transit.model.Stop;
import org.mad.transit.util.LocationsUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class MapFragment extends Fragment implements OnMapReadyCallback {

    static final String VIEW_MODEL_ARG = "VIEW_MODEL";
    static final int LOCATION_PERMISSIONS_REQUEST = 1234;
    private static final int INITIAL_ZOOM_VALUE = 16;
    private static final int MIN_ZOOM_VALUE = 14;
    private static final int MAX_ZOOM_VALUE = 18;
    GoogleMap googleMap;
    boolean followMyLocation;
    private boolean locationSettingsNotAvailable;
    private List<Marker> stopMarkers;
    private FusedLocationProviderClient fusedLocationProviderClient;
    LocationManager locationManager;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    final LatLng defaultLocation = new LatLng(45.254983, 19.844646); //Spomenik Svetozaru Miletiću, Novi Sad
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
    public void onDestroy() {
        super.onDestroy();
        if (this.locationSettingsChangedReceiver != null) {
            this.getActivity().unregisterReceiver(this.locationSettingsChangedReceiver);
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
        if (this.googleMap != null &&
                LocationsUtil.locationSettingsAvailability(this.locationManager) &&
                LocationsUtil.locationPermissionsGranted(this.getActivity())) {
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
                this.locationRequest = LocationsUtil.createLocationRequest();
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
        if (LocationsUtil.locationSettingsAvailability(this.locationManager)) {
            return true;
        } else {
            LocationsUtil.retrieveLocationSettings(this.locationRequest, this.getActivity(), this);
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LocationsUtil.LOCATION_REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_CANCELED) {
            this.locationSettingsNotAvailable = true;
        }
    }

    /**
     * Check if location permissions are granted to our app
     *
     * @return True if permissions are granted, otherwise false
     */
    private boolean areLocationPermissionsGranted() {
        if (LocationsUtil.locationPermissionsGranted(this.getActivity())) {
            return true;
        } else {
            LocationsUtil.requestPermissions(this);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSIONS_REQUEST && LocationsUtil.locationPermissionsGranted(this.getActivity())) {
            this.enableMyLocationAndLocationUpdates();
        } else {
            this.locationSettingsNotAvailable = true;
            View view = this.getActivity().findViewById(android.R.id.content);
            final Snackbar snackbar = Snackbar.make(view, R.string.location_permissions_not_available_message, Snackbar.LENGTH_SHORT);
            snackbar.setAction(R.string.dismiss_snack_bar, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }
    }

    public void zoomOnLocation(double latitude, double longitude) {
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

    public void addStopMarker(Stop stop) {
        Marker marker = this.googleMap.addMarker(new MarkerOptions()
                .title(stop.getTitle())
                .icon(this.bitmapDescriptorFromVector(R.drawable.ic_bus_marker))
                .position(new LatLng(stop.getLocation().getLatitude(), stop.getLocation().getLongitude())));
        marker.setTag(stop);
        if (this.stopMarkers == null) {
            this.stopMarkers = new ArrayList<>();
        }
        this.stopMarkers.add(marker);
    }

    BitmapDescriptor bitmapDescriptorFromVector(int vectorId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this.getActivity(), vectorId);
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
                    if (LocationsUtil.locationSettingsAvailability(MapFragment.this.locationManager)) {
                        MapFragment.this.enableMyLocation();
                    } else {
                        MapFragment.this.googleMap.setMyLocationEnabled(false);
                    }
                }
            }
        };

        this.getActivity().registerReceiver(this.locationSettingsChangedReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    public List<Marker> getStopMarkers() {
        if (this.stopMarkers == null) {
            this.stopMarkers = new ArrayList<>();
        }
        return this.stopMarkers;
    }

    public void clearStopMarkers() {
        this.googleMap.clear();
        this.stopMarkers = new ArrayList<>();
    }

    public void addPolyline(PolylineOptions polylineOptions) {
        this.googleMap.addPolyline(polylineOptions);
    }
}