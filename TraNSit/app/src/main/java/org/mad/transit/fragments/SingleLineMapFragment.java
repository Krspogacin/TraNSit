package org.mad.transit.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.view.model.SingleLineViewModel;

import java.util.List;

import javax.inject.Inject;

public class SingleLineMapFragment extends MapFragment {

    @Inject
    SingleLineViewModel singleLineViewModel;

    public static SingleLineMapFragment newInstance() {
        return new SingleLineMapFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {

        ((TransitApplication) this.getActivity().getApplicationContext()).getAppComponent().inject(this);

        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.registerLocationSettingsChangedReceiver();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        this.enableMyLocation();

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        for (Stop stop : this.singleLineViewModel.getStopsLiveData().getValue()) {
            this.addStopMarker(stop);
        }
        this.setPolyLineOnMap(this.singleLineViewModel.getLineLocations());

        View bottomSheetHeader = this.getActivity().findViewById(R.id.bottom_sheet_header);
        if (bottomSheetHeader != null) {
            View bottomSheet = this.getActivity().findViewById(R.id.bottom_sheet);
            this.putViewsAboveBottomSheet(bottomSheet, bottomSheetHeader.getHeight());
        }

        this.setOnInfoWindowClickListener();

//        List<Stop> stops = this.singleLineViewModel.getStopsLiveData().getValue();
//        if (stops != null) {
//            //zoomOnLocation(stops.get(0).getLocation().getLatitude(), stops.get(0).getLocation().getLongitude());
//        }
    }

    public void setPolyLineOnMap(List<Location> locations) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        for (Location location : locations) {
            polylineOptions.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        this.addPolyline(polylineOptions);
    }
}
