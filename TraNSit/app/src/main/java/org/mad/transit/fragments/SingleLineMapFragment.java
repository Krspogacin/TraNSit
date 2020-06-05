package org.mad.transit.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.mad.transit.R;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.view.model.SingleLineViewModel;

import java.util.List;

import androidx.lifecycle.ViewModelProvider;

public class SingleLineMapFragment extends MapFragment {

    private SingleLineViewModel singleLineViewModel;

    public static SingleLineMapFragment newInstance() {
        return new SingleLineMapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.singleLineViewModel = new ViewModelProvider(this).get(SingleLineViewModel.class);
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
            this.setPolyLineOnMap(singleLineViewModel.getLocationsLiveData().getValue());
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
                //zoomOnLocation(stops.get(0).getLocation().getLatitude(), stops.get(0).getLocation().getLongitude());
            }
        }
    }

    public void setPolyLineOnMap(List<Location> locations){
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        for (Location location : locations) {
            polylineOptions.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        this.addPolyline(polylineOptions);
    }
}
