package org.mad.transit.view.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.mad.transit.model.NearbyStop;
import org.mad.transit.repository.StopRepository;
import org.mad.transit.task.FindAllStopsAsyncTask;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;
import lombok.Setter;

@Singleton
public class StopViewModel extends ViewModel {

    @Getter
    @Setter
    private List<NearbyStop> allNearbyStops;
    private MutableLiveData<List<NearbyStop>> nearbyStopsLiveData;
    private final StopRepository stopRepository;

    @Inject
    public StopViewModel(StopRepository stopRepository) {
        this.stopRepository = stopRepository;
    }

    public MutableLiveData<List<NearbyStop>> getNearbyStopsLiveData() {
        if (this.nearbyStopsLiveData == null) {
            this.nearbyStopsLiveData = new MutableLiveData<>();
            new FindAllStopsAsyncTask(this.stopRepository, this).execute();
        }
        return this.nearbyStopsLiveData;
    }
}