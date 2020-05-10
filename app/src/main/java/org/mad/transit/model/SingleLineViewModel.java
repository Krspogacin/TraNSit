package org.mad.transit.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SingleLineViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<Stop>> lineStopsLiveData;
    private ArrayList<Stop> lineStops;

    public SingleLineViewModel() {
        this.lineStopsLiveData = new MutableLiveData<>();
        this.init();
    }

    public MutableLiveData<ArrayList<Stop>> getStopsLiveData() {
        return this.lineStopsLiveData;
    }

    private void init() {
        this.populateList();
        this.lineStopsLiveData.setValue(this.lineStops);
    }

    private void populateList() {

        Stop stop1 = Stop.builder()
                .name("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(3)))
                .latitude(45.261530)
                .longitude(19.836049)
                .build();
        Stop stop2 = Stop.builder()
                .name("Vojvode Bojovića - Socijalno")
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(2)))
                .latitude(45.258915)
                .longitude(19.837543)
                .build();
        Stop stop3 = Stop.builder()
                .name("Vojvode Bojovića - OŠ Ivo Lola Ribar")
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(2)))
                .latitude(45.258875)
                .longitude(19.837066)
                .build();

        Stop stop4 = Stop.builder()
                .name("Kisačka - Bulevar Kralja Petra I")
                .lines(Arrays.asList(LinesFragmentViewModel.getLines().get(0), LinesFragmentViewModel.getLines().get(3)))
                .latitude(45.262605)
                .longitude(19.839737)
                .build();

        this.lineStops = new ArrayList<>();
        this.lineStops.add(stop1);
        this.lineStops.add(stop2);
        this.lineStops.add(stop3);
        this.lineStops.add(stop4);
    }
}
