package org.mad.transit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timetable {
    private Line line;
    private LineDirection direction;
    private TimetableDay day;
}
