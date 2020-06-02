package org.mad.transit.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.mad.transit.R;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.view.model.SingleLineViewModel;

import java.util.List;

public class SingleLineMapFragment extends MapFragment {

    private SingleLineViewModel singleLineViewModel;
    private Polyline polylineA;
    private Polyline polylineB;

    public static SingleLineMapFragment newInstance(SingleLineViewModel singleLineViewModel) {
        SingleLineMapFragment singleLineMapFragment = new SingleLineMapFragment();

        Bundle args = new Bundle();
        args.putSerializable(VIEW_MODEL_ARG, singleLineViewModel);
        singleLineMapFragment.setArguments(args);

        return singleLineMapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            singleLineViewModel = (SingleLineViewModel) savedInstanceState.getSerializable(MapFragment.VIEW_MODEL_ARG);
        } else {
            singleLineViewModel = (SingleLineViewModel) getArguments().getSerializable(MapFragment.VIEW_MODEL_ARG);
        }
        registerLocationSettingsChangedReceiver();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        enableMyLocation();

        if (singleLineViewModel != null) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.RED);
            for (Stop stop : singleLineViewModel.getStopsLiveData().getValue()) {
                addStopMarker(stop);
            }
            this.setPolyLineOnMap(singleLineViewModel.getLocationsLiveData().getValue(), LineDirection.A);
        }

        View bottomSheetHeader = getActivity().findViewById(R.id.bottom_sheet_header);
        if (bottomSheetHeader != null) {
            View bottomSheet = getActivity().findViewById(R.id.bottom_sheet);
            putViewsAboveBottomSheet(bottomSheet, bottomSheetHeader.getHeight());
        }

        setOnInfoWindowClickListener();

        if (singleLineViewModel != null) {
            List<Stop> stops = singleLineViewModel.getStopsLiveData().getValue();
            if (stops != null) {
                zoomOnLocation(stops.get(0).getLocation().getLatitude(), stops.get(0).getLocation().getLongitude());
            }
        }
    }

    public void setPolyLineOnMap(List<Location> locations, LineDirection lineDirection) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        for (Location location : locations) {
            polylineOptions.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        Polyline polyline = this.addPolyline(polylineOptions);
        if (lineDirection == LineDirection.A) {
            this.polylineA = polyline;
        } else {
            this.polylineB = polyline;
        }
    }

    public Polyline getPolylineA() {
        return polylineA;
    }

    public Polyline getPolylineB() {
        return polylineB;
    }
}
