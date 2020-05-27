package org.mad.transit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineTimetableDto {
    private String name;
    private String direction;
    private TimetableDto timeTable;
}
