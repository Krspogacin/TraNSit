package org.mad.transit.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RoutePart {
    TravelType travelType;
    int duration;
    int lineNumber;
}
