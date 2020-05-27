package org.mad.transit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StopDto {
    private String lat;
    private String lon;
    private String name;
    private String zone;
}
