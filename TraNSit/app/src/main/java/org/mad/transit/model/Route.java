package org.mad.transit.model;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Route implements Serializable { // TODO remove after finishing routes and navigation
    private static final long serialVersionUID = 8943937020570681672L;
    int totalDuration;
    List<RoutePart> parts;
    String departureStop;
    String nextDeparture;
    int totalPrice;
}
