package org.mad.transit.task;

import android.os.AsyncTask;

import org.mad.transit.model.NearbyStop;
import org.mad.transit.model.Stop;
import org.mad.transit.repository.StopRepository;
import org.mad.transit.view.model.StopViewModel;

import java.util.ArrayList;
import java.util.List;

public class FindAllStopsAsyncTask extends AsyncTask<Void, Void, List<NearbyStop>> {

    private final StopRepository stopRepository;
    private final StopViewModel stopViewModel;

    public FindAllStopsAsyncTask(StopRepository stopRepository, StopViewModel stopViewModel) {
        this.stopRepository = stopRepository;
        this.stopViewModel = stopViewModel;
    }

    @Override
    protected List<NearbyStop> doInBackground(Void... voids) {
        List<Stop> stops = this.stopRepository.findAll();
        List<NearbyStop> nearbyStops = new ArrayList<>();
        for (Stop stop : stops) {
            NearbyStop nearbyStop = NearbyStop.builder().id(stop.getId())
                    .location(stop.getLocation())
                    .title(stop.getTitle())
                    .build();
            nearbyStops.add(nearbyStop);
        }

        return nearbyStops;
    }

    @Override
    protected void onPostExecute(List<NearbyStop> nearbyStops) {
        this.stopViewModel.setAllNearbyStops(nearbyStops);
        this.stopViewModel.getNearbyStopsLiveData().setValue(nearbyStops);
    }
}
