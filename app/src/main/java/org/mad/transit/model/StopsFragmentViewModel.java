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

        NearbyStop stop1 = new NearbyStop("Bulevar Kralja Petra I - Dom Zdravlja Zov", 3, new String[]{"8"});
        NearbyStop stop2 = new NearbyStop("Vojvode Bojovića - Socijalno", 4, new String[]{"3"});
        NearbyStop stop3 = new NearbyStop("Vojvode Bojovića - OŠ Ivo Lola Ribar", 5, new String[]{"3"});

        this.nearbyStops = new ArrayList<>();
        this.nearbyStops.add(stop1);
        this.nearbyStops.add(stop2);
        this.nearbyStops.add(stop3);
        this.nearbyStops.add(stop1);
        this.nearbyStops.add(stop2);
        this.nearbyStops.add(stop3);
    }
}