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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import org.mad.transit.R;
import org.mad.transit.activities.SingleStopActivity;
import org.mad.transit.model.DepartureTime;
import org.mad.transit.model.Line;
import org.mad.transit.model.LineOneDirection;
import org.mad.transit.model.Stop;
import org.mad.transit.model.Timetable;
import org.mad.transit.model.TimetableDay;
import org.mad.transit.repository.LineRepository;
import org.mad.transit.util.LocationsUtil;
import org.mad.transit.view.model.TimetableViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import lombok.SneakyThrows;

public abstract class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.forLanguageTag("sr-RS"));
    protected static final int LOCATION_PERMISSIONS_REQUEST = 1234;
    protected static final int INITIAL_ZOOM_VALUE = 16;
    protected static final int MIN_ZOOM_VALUE = 14;
    protected static final int MAX_ZOOM_VALUE = 18;
    protected GoogleMap googleMap;
    protected boolean followMyLocation;
    private boolean locationSettingsNotAvailable;
    private List<Marker> stopMarkers;
    private FusedLocationProviderClient fusedLocationProviderClient;
    protected LocationManager locationManager;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    protected final LatLng defaultLocation = new LatLng(45.254983, 19.844646); //Spomenik Svetozaru Miletiću, Novi Sad
    protected BroadcastReceiver locationSettingsChangedReceiver;
    private View bottomSheet;
    private int bottomSheetHeaderHeight;
    private View[] viewsToSlide;
    private float offset;
    protected Location currentLocation;

    @Inject
    LineRepository lineRepository;

    @Inject
    TimetableViewModel timetableViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (this.googleMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) MapFragment.this.getChildFragmentManager().findFragmentById(R.id.stops_map);
            mapFragment.getMapAsync(this);
        }

        if (this.locationSettingsNotAvailable) {
            this.locationSettingsNotAvailable = false;
        } else {
            this.enableMyLocationAndLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.stopLocationUpdates();
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

        this.bottomSheet = bottomSheet;
        this.bottomSheetHeaderHeight = bottomSheetHeaderHeight;
        this.viewsToSlide = viewsToSlide;

        this.googleMap.setPadding(0, 0, 0, bottomSheetHeaderHeight);

        for (View view : viewsToSlide) {
            view.setPadding(0, 0, 0, bottomSheetHeaderHeight);
        }

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                MapFragment.this.offset = slideOffset;
                MapFragment.this.setViewsPadding();
            }

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }
        });
    }

    void setViewsPadding() {
        if (this.bottomSheet == null) {
            return;
        }

        this.bottomSheet.measure(0, 0);
        int bottomSheetHeight = this.bottomSheet.getHeight();
//        int bottomSheetHeight = this.bottomSheet.getMeasuredHeight(); TODO check these 2 heights, there is a bug with padding when there is less items in the bottom sheet

        int realBottomSheetOffset = (int) (this.bottomSheetHeaderHeight + (bottomSheetHeight - this.bottomSheetHeaderHeight) * this.offset);
        if (realBottomSheetOffset > 0) {
            MapFragment.this.googleMap.setPadding(0, 0, 0, realBottomSheetOffset);
            for (View view : this.viewsToSlide) {
                view.setPadding(0, 0, 0, realBottomSheetOffset);
            }
        }
    }

    void setOnInfoWindowClickListener() {

        if (this.googleMap == null) {
            return;
        }

        this.googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @SneakyThrows
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MapFragment.this.getContext(), SingleStopActivity.class);
                Stop stop = (Stop) marker.getTag();

                if (stop == null) {
                    return;
                }

                //Retrieve all lines available at this stop
                if (stop.getLines() == null) {
                    stop.setLines(MapFragment.this.lineRepository.findAllByStopId(stop.getId()));

                    for (Line line : stop.getLines()) {
                        LineOneDirection lineOneDirection;
                        if (line.getLineDirectionA() != null) {
                            lineOneDirection = line.getLineDirectionA();
                        } else {
                            lineOneDirection = line.getLineDirectionB();
                            final String[] lineStations = line.getTitle().split("-");
                            Collections.reverse(Arrays.asList(lineStations));
                            StringBuilder lineTitleBuilder = new StringBuilder();
                            for (int i = 0; i < lineStations.length; i++) {
                                lineTitleBuilder.append(lineStations[i].trim());
                                if (i != lineStations.length - 1) {
                                    lineTitleBuilder.append(" - ");
                                }
                            }
                            line.setTitle(lineTitleBuilder.toString());
                        }
                        lineOneDirection.setTimetablesMap(MapFragment.this.timetableViewModel.findAllByLineIdAndLineDirection(line.getId(), lineOneDirection.getLineDirection()));
                    }
                }

                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                TimetableDay timetableDay;

                switch (day) {
                    case Calendar.SUNDAY:
                        timetableDay = TimetableDay.SUNDAY;
                        break;
                    case Calendar.SATURDAY:
                        timetableDay = TimetableDay.SATURDAY;
                        break;
                    default:
                        timetableDay = TimetableDay.WORKDAY;
                }

                for (Line line : stop.getLines()) {
                    LineOneDirection lineOneDirection;
                    if (line.getLineDirectionA() != null) {
                        lineOneDirection = line.getLineDirectionA();
                    } else {
                        lineOneDirection = line.getLineDirectionB();
                    }

                    Timetable timetable = lineOneDirection.getTimetablesMap().get(timetableDay.toString());
                    Date currentTime = MapFragment.dateFormat.parse(MapFragment.dateFormat.format(new Date()));

                    if (timetable != null) {
                        StringBuilder nextDeparturesBuilder = new StringBuilder();

                        int counter = 0;
                        for (DepartureTime departureTimeObject : timetable.getDepartureTimes()) {
                            Date departureTime = MapFragment.dateFormat.parse(departureTimeObject.getFormattedValue());

                            if (departureTime == null) {
                                continue;
                            }

                            if (departureTime.after(currentTime)) {
                                nextDeparturesBuilder.append(departureTimeObject.getFormattedValue()).append(", ");
                                counter++;
                            }

                            if (counter == 3) {
                                break;
                            }
                        }

                        String nextDepartures = nextDeparturesBuilder.toString();
                        if (nextDepartures.endsWith(", ")) {
                            nextDepartures = nextDepartures.substring(0, nextDepartures.length() - 2);
                        }
                        line.setNextDepartures(nextDepartures);
                    }
                }

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
                    MapFragment.this.currentLocation = lastLocation;
                    if (lastLocation != null && MapFragment.this.followMyLocation) {
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

    void stopLocationUpdates() {
        if (this.googleMap != null) {

            this.googleMap.setMyLocationEnabled(false);

            if (this.locationCallback != null) {
                this.fusedLocationProviderClient.removeLocationUpdates(this.locationCallback);
                this.currentLocation = null;
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
            snackbar.setAction(R.string.dismiss_snack_bar, v -> snackbar.dismiss());
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

    public void zoomOnDefaultBounds() {
        LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
        LatLng latLng1 = new LatLng(45.273676, 19.770105);
        LatLng latLng2 = new LatLng(45.226507, 19.891183);
        boundsBuilder.include(latLng1);
        boundsBuilder.include(latLng2);
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 0));
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

    public void setStopMarkers(List<Marker> stopMarkers) {
        this.stopMarkers = stopMarkers;
    }

    public void clearMap() {
        this.googleMap.clear();
        this.stopMarkers = new ArrayList<>();
    }

    public Polyline addPolyline(PolylineOptions polylineOptions) {
        return this.googleMap.addPolyline(polylineOptions);
    }

    public void addLocationMarker(org.mad.transit.model.Location location) {
        this.googleMap.addMarker(new MarkerOptions()
                .title(location.getName())
                .position(new LatLng(location.getLatitude(), location.getLongitude())));
    }

    public void expandBottomSheet() {
        if (this.bottomSheet != null) {
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
}