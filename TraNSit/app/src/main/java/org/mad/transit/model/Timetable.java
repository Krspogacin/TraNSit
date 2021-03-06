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
public class Timetable implements Serializable {
    private static final long serialVersionUID = 2495583793796693089L;
    private Long id;
    private Long lineId;
    private LineDirection direction;
    private TimetableDay day;
    private List<DepartureTime> departureTimes;

    public List<DepartureTime> getDepartureTimes() {
        return this.departureTimes;
    }
}