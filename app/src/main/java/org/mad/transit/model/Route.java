package org.mad.transit.model;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Route {
    int totalDuration;
    List<RoutePart> parts;
    String departureStop;
    String nextDeparture;
    int totalPrice;
}
