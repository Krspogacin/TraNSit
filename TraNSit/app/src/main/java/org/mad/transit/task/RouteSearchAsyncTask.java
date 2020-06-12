package org.mad.transit.task;

import android.os.AsyncTask;

import org.mad.transit.model.Location;
import org.mad.transit.search.Route;
import org.mad.transit.search.RouteSearchProblem;
import org.mad.transit.search.SearchService;

import java.util.List;

public class RouteSearchAsyncTask extends AsyncTask<Void, Void, List<Route>> {

    private Location startLocation;
    private Location endLocation;
    private SearchService searchService;
    private TaskListener taskListener;

    public RouteSearchAsyncTask(Location startLocation, Location endLocation, SearchService searchService, TaskListener taskListener) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.searchService = searchService;
        this.taskListener = taskListener;
    }

    @Override
    protected List<Route> doInBackground(Void... voids) {
        RouteSearchProblem problem = new RouteSearchProblem(startLocation, endLocation);
        return searchService.searchRoutes(problem);
    }

    @Override
    protected void onPostExecute(List<Route> routes) {
        taskListener.onFinished(routes);
    }
}
