package org.mad.transit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceList {
    private Long id;
    private Zone firstStationZone;
    private Zone secondStationZone;
    private Double price;
}
