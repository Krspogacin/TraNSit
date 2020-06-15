package org.mad.transit.search;

import org.mad.transit.model.Location;

import java.util.Calendar;

import lombok.Data;

@Data
public class RouteSearchProblem {
    private Location startLocation;
    private Location endLocation;
    private long startTime;

    public RouteSearchProblem(Location startLocation, Location endLocation) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        this.startTime = (60 * hours + minutes) * 60_000L;
    }

    public SearchState getStartState() {
        return new SearchState(startLocation, 0);
    }

    public boolean isEndState(SearchState currentState) {
        return currentState.getLocation().equals(endLocation);
    }
}
