package org.mad.transit.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StopsViewModel extends ViewModel implements Serializable {
    private static final long serialVersionUID = 1141690979133418351L;
    private final List<NearbyStop> nearbyStops;

    public StopsViewModel() {
        this.nearbyStops = new ArrayList<>();
        this.populateList();
    }

    public MutableLiveData<List<NearbyStop>> getNearbyStopsLiveData() {
        return new MutableLiveData<>(this.nearbyStops);
    }

    private void populateList() {

        NearbyStop stop1 = NearbyStop.builder()
                .title("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .walkTime(3)
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(3)))
                .coordinate(new Coordinate(45.261530, 19.836049))
                .build();

        NearbyStop stop2 = NearbyStop.builder()
                .title("Vojvode Bojovića - Socijalno")
                .walkTime(4)
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(2)))
                .coordinate(new Coordinate(45.258915, 19.837543))
                .build();

        NearbyStop stop3 = NearbyStop.builder()
                .title("Vojvode Bojovića - OŠ Ivo Lola Ribar")
                .walkTime(5)
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(2)))
                .coordinate(new Coordinate(45.258875, 19.837066))
                .build();

        NearbyStop stop4 = NearbyStop.builder()
                .title("Kisačka - Bulevar Kralja Petra I")
                .walkTime(5)
                .lines(Arrays.asList(LinesFragmentViewModel.getLines().get(0), LinesFragmentViewModel.getLines().get(3)))
                .coordinate(new Coordinate(45.262605, 19.839737))
                .build();

        this.nearbyStops.add(stop1);
        this.nearbyStops.add(stop2);
        this.nearbyStops.add(stop3);
        this.nearbyStops.add(stop4);
    }
}