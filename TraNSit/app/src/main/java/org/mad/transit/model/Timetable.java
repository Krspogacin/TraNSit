package org.mad.transit.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timetable {
    private Long id;
    private Line line;
    private LineDirection direction;
    private TimetableDay day;
    private List<DepartureTime> departureTimes;
}
