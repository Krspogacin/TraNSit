package org.mad.transit.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.mad.transit.R;
import org.mad.transit.model.SingleLineViewModel;
import org.mad.transit.model.Stop;

import java.util.List;

public class SingleLineMapFragment extends MapFragment {

    private SingleLineViewModel singleLineViewModel;

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
            List<Stop> stops = singleLineViewModel.getStopsLiveData().getValue();
            if (stops != null) {
                for (Stop stop : stops) {
                    addStopMarker(stop);
                    polylineOptions.add(new LatLng(stop.getLocation().getLatitude(), stop.getLocation().getLongitude()));
                }
            }
            this.addPolyline(polylineOptions);
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
}