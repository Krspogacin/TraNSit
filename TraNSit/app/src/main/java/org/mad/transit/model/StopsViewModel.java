package org.mad.transit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
                .location(new Location(45.261530, 19.836049))
                .build();

        NearbyStop stop2 = NearbyStop.builder()
                .title("Vojvode Bojovića - Socijalno")
                .walkTime(4)
                .location(new Location(45.258915, 19.837543))
                .build();

        NearbyStop stop3 = NearbyStop.builder()
                .title("Vojvode Bojovića - OŠ Ivo Lola Ribar")
                .walkTime(5)
                .location(new Location(45.258875, 19.837066))
                .build();

        NearbyStop stop4 = NearbyStop.builder()
                .title("Kisačka - Bulevar Kralja Petra I")
                .walkTime(5)
                .location(new Location(45.262605, 19.839737))
                .build();

        this.nearbyStops.add(stop1);
        this.nearbyStops.add(stop2);
        this.nearbyStops.add(stop3);
        this.nearbyStops.add(stop4);
    }
}