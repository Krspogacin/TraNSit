package org.mad.transit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartureTime {
    private Long id;
    private Integer hours;
    private Integer minutes;
    private String formattedValue;
    private Timetable timetable;
}
