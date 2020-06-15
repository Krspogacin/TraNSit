package org.mad.transit.view.model;

import android.util.Log;

import org.mad.transit.dto.RouteDto;
import org.mad.transit.model.Location;
import org.mad.transit.search.SearchService;
import org.mad.transit.task.RouteSearchAsyncTask;
import org.mad.transit.task.TaskListener;

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
        final long startMS = System.currentTimeMillis();
        new RouteSearchAsyncTask(startLocation, endLocation, searchService, new TaskListener() {
            @Override
            public void onFinished(Object result) {
                long endMS = System.currentTimeMillis();
                Log.i("SPENT TIME", String.valueOf(endMS - startMS));
                List<RouteDto> routes = (List<RouteDto>) result;
                for (RouteDto route : routes) {
                    System.out.println(route);
                }
                RouteViewModel.this.routes = routes;
                RouteViewModel.this.routesLiveData.setValue(routes);
            }
        }).execute();
    }
}
