package org.mad.transit.view.model;

import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SingleLineViewModel extends ViewModel implements Serializable {
    private static final long serialVersionUID = -469369339539678360L;
    private List<Stop> lineStops;
    private List<Location> lineLocations;

    public SingleLineViewModel() {
        this.lineStops = new ArrayList<>();
        this.lineLocations = new ArrayList<>();
    }

    public MutableLiveData<List<Stop>> getStopsLiveData() {
        return new MutableLiveData<>(this.lineStops);
    }

    public void setLineStops(List<Stop> stops) {
        this.lineStops = stops;
    }

    public MutableLiveData<List<Location>> getLocationsLiveData() {
        return new MutableLiveData<>(this.lineLocations);
    }

    public void setLineLocations(List<Location> locations) {
        this.lineLocations = locations;
    }
}
