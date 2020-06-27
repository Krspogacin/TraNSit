package org.mad.transit.view.model;

import org.mad.transit.dto.RouteDto;
import org.mad.transit.model.Location;
import org.mad.transit.search.SearchOptions;
import org.mad.transit.search.SearchService;
import org.mad.transit.task.RouteSearchAsyncTask;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static org.mad.transit.search.RouteComparator.getDefaultComparator;

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

    public void findRoutes(Location startLocation, Location endLocation, SearchOptions searchOptions) {
        new RouteSearchAsyncTask(startLocation, endLocation, searchOptions, searchService, result -> {
            List<RouteDto> foundRoutes = (List<RouteDto>) result;
            Collections.sort(foundRoutes, getDefaultComparator());
            RouteViewModel.this.routes = foundRoutes;
            RouteViewModel.this.routesLiveData.setValue(foundRoutes);
        }).execute();
    }

    public void sortRoutes(Comparator<RouteDto> routeComparator) {
        Collections.sort(this.routes, routeComparator);
        this.routesLiveData.setValue(this.routes);
    }
}
