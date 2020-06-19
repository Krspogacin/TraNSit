package org.mad.transit.search;

import org.mad.transit.model.Line;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.util.LocationsUtil;

import lombok.Data;

@Data
public class SearchState {
    private Location location;
    private Stop stop;
    private Line line;
    private LineDirection lineDirection;
    private double timeElapsed;

    public SearchState(Location location, double timeElapsed) {
        this.location = location;
        this.timeElapsed = timeElapsed;
    }

    public Long getTimeElapsedInMilliseconds() {
        return Math.round(timeElapsed * 3_600_000);
    }

    public double aStarCost(Location endLocation) {
        return timeElapsed + heuristic(endLocation);
    }

    private double heuristic(Location endLocation) {
        return LocationsUtil.calculateDistance(location.getLatitude(), location.getLongitude(),
                endLocation.getLatitude(), endLocation.getLongitude()) / 29;
    }
}
