package org.mad.transit.fragments;

import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;

import org.mad.transit.model.NearbyStop;
import org.mad.transit.model.StopsViewModel;

import java.util.List;

public class StopsMapFragment extends MapFragment {

    private StopsViewModel stopsViewModel;

    public static StopsMapFragment newInstance(StopsViewModel stopsViewModel) {
        StopsMapFragment stopsMapFragment = new StopsMapFragment();

        Bundle args = new Bundle();
        args.putSerializable(VIEW_MODEL_ARG, stopsViewModel);
        stopsMapFragment.setArguments(args);

        return stopsMapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO make case savedInstanceState != null to use arguments from previous destroyed fragment instance (https://stackoverflow.com/a/37410735)
        stopsViewModel = (StopsViewModel) getArguments().getSerializable(VIEW_MODEL_ARG);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        configAboutFloatingLocationButton();

        if (stopsViewModel != null) {
            List<NearbyStop> nearbyStops = stopsViewModel.getNearbyStopsLiveData().getValue();
            if (nearbyStops != null) {
                for (NearbyStop nearbyStop : nearbyStops) {
                    addStopMarker(nearbyStop);
                }
            }
        }
    }
}
