package org.mad.transit.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

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
                .lines(new String[]{"8"})
                .latitude(45.261530)
                .longitude(19.836049)
                .build();
        NearbyStop stop2 = NearbyStop.builder()
                .name("Vojvode Bojovića - Socijalno")
                .walkTime(4)
                .lines(new String[]{"3"})
                .latitude(45.258915)
                .longitude(19.837543)
                .build();
        NearbyStop stop3 = NearbyStop.builder()
                .name("Vojvode Bojovića - OŠ Ivo Lola Ribar")
                .walkTime(5)
                .lines(new String[]{"3"})
                .latitude(45.258875)
                .longitude(19.837066)
                .build();

        this.nearbyStops = new ArrayList<>();
        this.nearbyStops.add(stop1);
        this.nearbyStops.add(stop2);
        this.nearbyStops.add(stop3);
        this.nearbyStops.add(stop1);
        this.nearbyStops.add(stop2);
        this.nearbyStops.add(stop3);
    }
}