package org.mad.transit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
                .title("Bulevar Kralja Petra I - Sajam")
                .coordinate(new Coordinate(45.259119, 19.824429))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();

        Stop stop2 = Stop.builder()
                .title("Bulevar Kralja Petra I - Mašinska Škola")
                .coordinate(new Coordinate(45.259440, 19.827440))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();


        Stop stop3 = Stop.builder()
                .title("Bulevar Kralja Petra I - Bulevar Oslobođenja")
                .coordinate(new Coordinate(45.260742, 19.832810))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();

        Stop stop4 = Stop.builder()
                .title("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .coordinate(new Coordinate(45.261530, 19.836049))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();

        this.lineStops.add(stop1);
        this.lineStops.add(stop2);
        this.lineStops.add(stop3);
        this.lineStops.add(stop4);
    }
}
