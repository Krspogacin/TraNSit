package org.mad.transit.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineOneDirection implements Serializable {
    private static final long serialVersionUID = -4561684211824087919L;
    private LineDirection lineDirection;
    private List<Stop> stops;
    private List<Location> locations;
    private Map<String, Timetable> timetablesMap;

    public LineOneDirection(LineDirection lineDirection, List<Stop> stops, List<Location> locations) {
        this.lineDirection = lineDirection;
        this.stops = stops;
        this.locations = locations;
    }
}
