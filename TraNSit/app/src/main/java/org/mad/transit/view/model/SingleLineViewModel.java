package org.mad.transit.view.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.repository.LocationRepository;
import org.mad.transit.repository.StopRepository;
import org.mad.transit.task.FindAllLineLocationsAndStopsAsyncTask;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SingleLineViewModel extends ViewModel {

    private MutableLiveData<List<Stop>> linesStopsLiveData;
    private List<Location> lineLocations;
    private final StopRepository stopRepository;
    private final LocationRepository locationRepository;

    @Inject
    public SingleLineViewModel(StopRepository stopRepository, LocationRepository locationRepository) {
        this.stopRepository = stopRepository;
        this.locationRepository = locationRepository;
    }

    public MutableLiveData<List<Stop>> getStopsLiveData() {
        if (this.linesStopsLiveData == null) {
            this.linesStopsLiveData = new MutableLiveData<>();
        }
        return this.linesStopsLiveData;
    }

    public List<Location> getLineLocations() {
        if (this.lineLocations == null) {
            this.lineLocations = new ArrayList<>();
        }
        return this.lineLocations;
    }

    public void findAllStopsAndLocationsByLineIdAndLineDirection(Long lineId, LineDirection lineDirection) {
        new FindAllLineLocationsAndStopsAsyncTask(this.stopRepository, this.locationRepository,this, lineId, lineDirection).execute();
    }

    public void setLineLocations(List<Location> lineLocations) {
        this.lineLocations = lineLocations;
    }
}
