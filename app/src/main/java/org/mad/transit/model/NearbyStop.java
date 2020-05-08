package org.mad.transit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyStop {
    private String name;
    private int walkTime;
    private String[] lines;
    private double latitude;
    private double longitude;
}