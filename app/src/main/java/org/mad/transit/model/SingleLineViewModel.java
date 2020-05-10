package org.mad.transit.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SingleLineViewModel extends ViewModel implements Serializable {
    private static final long serialVersionUID = -469369339539678360L;
    private final List<Stop> lineStops;

    public SingleLineViewModel() {
        this.lineStops = new ArrayList<>();
        this.populateList();
    }

    public MutableLiveData<List<Stop>> getStopsLiveData() {
        return new MutableLiveData<>(this.lineStops);
    }

    private void populateList() {

        Stop stop1 = Stop.builder()
                .title("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(3)))
                .latitude(45.261530)
                .longitude(19.836049)
                .build();
        Stop stop2 = Stop.builder()
                .title("Vojvode Bojovića - Socijalno")
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(2)))
                .latitude(45.258915)
                .longitude(19.837543)
                .build();
        Stop stop3 = Stop.builder()
                .title("Vojvode Bojovića - OŠ Ivo Lola Ribar")
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(2)))
                .latitude(45.258875)
                .longitude(19.837066)
                .build();

        Stop stop4 = Stop.builder()
                .title("Kisačka - Bulevar Kralja Petra I")
                .lines(Arrays.asList(LinesFragmentViewModel.getLines().get(0), LinesFragmentViewModel.getLines().get(3)))
                .latitude(45.262605)
                .longitude(19.839737)
                .build();

        this.lineStops.add(stop1);
        this.lineStops.add(stop2);
        this.lineStops.add(stop3);
        this.lineStops.add(stop4);
    }
}
