package org.mad.transit.search;

import org.mad.transit.model.Location;
import org.mad.transit.util.LocationsUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Action {
    private Location startLocation;
    private Location endLocation;

    public SearchState execute(SearchState currentState) {
        return new SearchState(endLocation, currentState.getTimeElapsed() + timeCost());
    }

    protected double timeCost() {
        return distance() / velocity();
    }

    protected double distance() {
        return LocationsUtil.calculateDistance(startLocation.getLatitude(), startLocation.getLongitude(),
                endLocation.getLatitude(), endLocation.getLongitude());
    }

    protected abstract int velocity();
}
