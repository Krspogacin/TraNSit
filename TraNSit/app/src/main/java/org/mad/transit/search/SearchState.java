package org.mad.transit.search;

import org.mad.transit.model.Line;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.util.LocationsUtil;

import lombok.Data;

import static org.mad.transit.util.Constants.MILLISECONDS_IN_HOUR;

@Data
public class SearchState {
    private static final double TRAVEL_COST_FACTOR = 65 * 10D; // 65 is the cost for zone I and for other zones when travelling inside them
    private static final int HEURISTIC_FACTOR = 29; // random stuff
    private Location location;
    private Stop stop;
    private Line line;
    private LineDirection lineDirection;
    private double timeElapsed;
    private int travelCost;

    public SearchState(Location location, double timeElapsed, int travelCost) {
        this.location = location;
        this.timeElapsed = timeElapsed;
        this.travelCost = travelCost;
    }

    public Long getTimeElapsedInMilliseconds() {
        return Math.round(timeElapsed * MILLISECONDS_IN_HOUR);
    }

    public double aStarCost(Location endLocation) {
        return timeElapsed + travelCost() + heuristic(endLocation);
    }

    private double travelCost() {
        return travelCost / TRAVEL_COST_FACTOR;
    }

    private double heuristic(Location endLocation) {
        return LocationsUtil.calculateDistance(location.getLatitude(), location.getLongitude(),
                endLocation.getLatitude(), endLocation.getLongitude()) / HEURISTIC_FACTOR;
    }
}
