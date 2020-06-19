package org.mad.transit.view.model;

import org.mad.transit.dto.RouteDto;
import org.mad.transit.model.Location;
import org.mad.transit.search.SearchService;
import org.mad.transit.task.RouteSearchAsyncTask;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RouteViewModel extends ViewModel {

    private List<RouteDto> routes;
    private MutableLiveData<List<RouteDto>> routesLiveData;
    private SearchService searchService;

    public RouteViewModel(SearchService searchService) {
        this.searchService = searchService;
    }

    public MutableLiveData<List<RouteDto>> getRoutesLiveData() {
        if (this.routesLiveData == null) {
            this.routesLiveData = new MutableLiveData<>();
            this.routesLiveData.setValue(this.routes);
        }
        return this.routesLiveData;
    }

    public void findRoutes(Location startLocation, Location endLocation) {
        new RouteSearchAsyncTask(startLocation, endLocation, searchService, result -> {
            List<RouteDto> routes = (List<RouteDto>) result;
            RouteViewModel.this.routes = routes;
            RouteViewModel.this.routesLiveData.setValue(routes);
        }).execute();
    }
}
