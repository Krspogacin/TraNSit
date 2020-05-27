package org.mad.transit.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineOneDirection implements Serializable {
    private LineDirection lineDirection;
    private List<Stop> stops;
    private List<Location> locations;
}
