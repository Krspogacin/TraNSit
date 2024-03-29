package org.mad.transit.task;

import android.os.AsyncTask;

import org.mad.transit.dto.RouteDto;
import org.mad.transit.model.Location;
import org.mad.transit.search.RouteSearchProblem;
import org.mad.transit.search.SearchOptions;
import org.mad.transit.search.SearchService;

import java.util.List;

public class RouteSearchAsyncTask extends AsyncTask<Void, Void, List<RouteDto>> {

    private Location startLocation;
    private Location endLocation;
    private SearchOptions searchOptions;
    private SearchService searchService;
    private TaskListener taskListener;

    public RouteSearchAsyncTask(Location startLocation, Location endLocation, SearchOptions searchOptions,
                                SearchService searchService, TaskListener taskListener) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.searchOptions = searchOptions;
        this.searchService = searchService;
        this.taskListener = taskListener;
    }

    @Override
    protected List<RouteDto> doInBackground(Void... voids) {
        RouteSearchProblem problem = new RouteSearchProblem(startLocation, endLocation, searchOptions);
        return searchService.searchRoutes(problem);
    }

    @Override
    protected void onPostExecute(List<RouteDto> routes) {
        taskListener.onFinished(routes);
    }
}
