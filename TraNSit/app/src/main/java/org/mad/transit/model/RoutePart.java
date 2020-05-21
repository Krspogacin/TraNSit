package org.mad.transit.model;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RoutePart implements Serializable {
    private static final long serialVersionUID = 3398517739172373029L;
    TravelType travelType;
    int duration;
    int lineNumber;
    List<Stop> stops;
}
