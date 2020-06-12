package org.mad.transit.search;


import org.mad.transit.model.LineDirection;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LineStopDirection {
    private Long lineId;
    private Long stopId;
    private LineDirection direction;
}
