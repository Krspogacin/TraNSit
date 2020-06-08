package org.mad.transit.task;

import android.os.AsyncTask;

import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.repository.LocationRepository;
import org.mad.transit.repository.StopRepository;
import org.mad.transit.view.model.SingleLineViewModel;

import java.util.List;

public class FindAllLineLocationsAndStopsAsyncTask extends AsyncTask<Void, Void, Void> {
    private StopRepository stopRepository;
    private LocationRepository locationRepository;
    private Long lineId;
    private  LineDirection lineDirection;
    private SingleLineViewModel viewModel;
    private List<Stop> stops;
    private List<Location> locations;

    public FindAllLineLocationsAndStopsAsyncTask(StopRepository stopRepository, LocationRepository locationRepository, SingleLineViewModel viewModel, Long lineId, LineDirection lineDirection){
        this.stopRepository = stopRepository;
        this.locationRepository = locationRepository;
        this.lineId = lineId;
        this.lineDirection = lineDirection;
        this.viewModel = viewModel;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        this.stops = this.stopRepository.findAllByLineIdAndLineDirection(lineId, lineDirection);
        this.locations = this.locationRepository.findAllByLineIdAndLineDirection(lineId, lineDirection);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        this.viewModel.setLineLocations(locations);
        this.viewModel.getStopsLiveData().setValue(stops);
    }
}
