package org.mad.transit.fragments;

import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

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
        //TODO make case savedInstanceState != null to use arguments from previous destroyed fragment instance (https://stackoverflow.com/a/37410735)
        singleLineViewModel = (SingleLineViewModel) getArguments().getSerializable(VIEW_MODEL_ARG);
        this.registerLocationSettingsChangedReceiver();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        this.enableMyLocation();

        if (singleLineViewModel != null) {
            List<Stop> stops = singleLineViewModel.getStopsLiveData().getValue();
            if (stops != null) {
                for (Stop stop : stops) {
                    addStopMarker(stop);
                }
            }
        }

        View bottomSheetHeader = this.getActivity().findViewById(R.id.bottom_sheet_header);
        if (bottomSheetHeader != null) {
            View bottomSheet = this.getActivity().findViewById(R.id.bottom_sheet);
            this.putViewsAboveBottomSheet(bottomSheet, bottomSheetHeader.getHeight());
        }

        this.setOnInfoWindowClickListener();

        if (singleLineViewModel != null){
            List<Stop> stops = singleLineViewModel.getStopsLiveData().getValue();
            if (stops != null) {
                this.zoomOnLocation(stops.get(0).getLatitude(), stops.get(0).getLongitude());
            }
        }
    }
}
