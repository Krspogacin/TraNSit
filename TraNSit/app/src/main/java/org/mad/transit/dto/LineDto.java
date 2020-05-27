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
public class LineDto {
    private String title;
    private String name;
    private String type;
    private String direction;
    private List<LocationDto> coordinates;
}
