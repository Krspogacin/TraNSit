package org.mad.transit.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class StopsFragmentViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<NearbyStop>> nearbyStopsLiveData;
    private ArrayList<NearbyStop> nearbyStops;

    public StopsFragmentViewModel() {
        this.nearbyStopsLiveData = new MutableLiveData<>();
        this.init();
    }

    public MutableLiveData<ArrayList<NearbyStop>> getNearbyStopsLiveData() {
        return this.nearbyStopsLiveData;
    }

    private void init() {
        this.populateList();
        this.nearbyStopsLiveData.setValue(this.nearbyStops);
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

        this.nearbyStops = new ArrayList<>();
        this.nearbyStops.add(stop1);
        this.nearbyStops.add(stop2);
        this.nearbyStops.add(stop3);
        this.nearbyStops.add(stop4);
    }
}