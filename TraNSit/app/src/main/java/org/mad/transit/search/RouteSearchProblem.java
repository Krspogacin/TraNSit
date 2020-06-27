package org.mad.transit.search;

import org.mad.transit.model.Location;

import lombok.Data;

import static org.mad.transit.util.Constants.MILLISECONDS_IN_MINUTE;

@Data
public class RouteSearchProblem {
    private Location startLocation;
    private Location endLocation;
    private int solutionCount;
    private boolean transfersEnabled;
    private long startTime;

    public RouteSearchProblem(Location startLocation, Location endLocation, SearchOptions searchOptions) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.solutionCount = searchOptions.getSolutionCount();
        this.transfersEnabled = searchOptions.isTransfersEnabled();
        this.startTime = (60 * searchOptions.getHours() + searchOptions.getMinutes()) * MILLISECONDS_IN_MINUTE;
    }

    public SearchState getStartState() {
        return new SearchState(startLocation, 0, 0);
    }

    public boolean isEndState(SearchState currentState) {
        return currentState.getLocation().equals(endLocation);
    }
}
