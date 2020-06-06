package org.mad.transit.view.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.repository.LocationRepository;
import org.mad.transit.repository.StopRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SingleLineViewModel extends ViewModel {

    private MutableLiveData<List<Stop>> linesStopsLiveData;
    private final StopRepository stopRepository;
    private final LocationRepository locationRepository;

    private List<Location> lineLocations;

    @Inject
    public SingleLineViewModel(StopRepository stopRepository, LocationRepository locationRepository) {
        this.stopRepository = stopRepository;
        this.locationRepository = locationRepository;
    }

    public MutableLiveData<List<Stop>> getStopsLiveData() {
        if (this.linesStopsLiveData == null) {
            this.linesStopsLiveData = new MutableLiveData<>();
            this.linesStopsLiveData.setValue(new ArrayList<Stop>());
        }
        return this.linesStopsLiveData;
    }

    public List<Stop> findAllStopsByLineIdAndLineDirection(Long lineId, LineDirection lineDirection) {
        return this.stopRepository.findAllByLineIdAndLineDirection(lineId, lineDirection);
    }

    public List<Location> findAllLocationsByLineIdAndLineDirection(Long lineId, LineDirection lineDirection) {
        List<Location> allByLineIdAndLineDirection = this.locationRepository.findAllByLineIdAndLineDirection(lineId, lineDirection);
        this.lineLocations = new ArrayList<>(allByLineIdAndLineDirection);
        return allByLineIdAndLineDirection;
    }

    public List<Location> getLineLocations() {
        if (this.lineLocations == null) {
            this.lineLocations = new ArrayList<>();
        }
        return this.lineLocations;
    }
}
