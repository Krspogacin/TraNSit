package org.mad.transit.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineStopsDto {
    private String name;
    private String direction;
    private List<StopDto> stops;
}
