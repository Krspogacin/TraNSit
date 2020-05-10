package org.mad.transit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StopsViewModel extends ViewModel implements Serializable {
    private static final long serialVersionUID = 1141690979133418351L;
    private final List<NearbyStop> nearbyStops;

    public StopsViewModel() {
        nearbyStops = new ArrayList<>();
        populateList();
    }

    public MutableLiveData<List<NearbyStop>> getNearbyStopsLiveData() {
        return new MutableLiveData<>(nearbyStops);
    }

    private void populateList() {

        NearbyStop stop1 = NearbyStop.builder()
                .name("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .walkTime(3)
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(3)))
                .latitude(45.261530)
                .longitude(19.836049)
                .build();
        NearbyStop stop2 = NearbyStop.builder()
                .name("Vojvode Bojovića - Socijalno")
                .walkTime(4)
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(2)))
                .latitude(45.258915)
                .longitude(19.837543)
                .build();
        NearbyStop stop3 = NearbyStop.builder()
                .name("Vojvode Bojovića - OŠ Ivo Lola Ribar")
                .walkTime(5)
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(2)))
                .latitude(45.258875)
                .longitude(19.837066)
                .build();

        NearbyStop stop4 = NearbyStop.builder()
                .name("Kisačka - Bulevar Kralja Petra I")
                .walkTime(5)
                .lines(Arrays.asList(LinesFragmentViewModel.getLines().get(0), LinesFragmentViewModel.getLines().get(3)))
                .latitude(45.262605)
                .longitude(19.839737)
                .build();

        nearbyStops.add(stop1);
        nearbyStops.add(stop2);
        nearbyStops.add(stop3);
        nearbyStops.add(stop4);
    }
}