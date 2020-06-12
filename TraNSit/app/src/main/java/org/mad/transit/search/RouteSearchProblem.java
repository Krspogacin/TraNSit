package org.mad.transit.search;

import org.mad.transit.model.Location;

import lombok.Data;

@Data
public class RouteSearchProblem {
    private Location startLocation;
    private Location endLocation;
    private long startTime;

    public RouteSearchProblem(Location startLocation, Location endLocation) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.startTime = System.currentTimeMillis();
    }

    public SearchState getStartState() {
        return new SearchState(startLocation, 0);
    }

    public boolean isEndState(SearchState currentState) {
        return currentState.getLocation().equals(endLocation);
    }
}
