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
public class NearbyStop implements Serializable {
    private static final long serialVersionUID = 9663211053934640L;
    private String name;
    private int walkTime;
    private List<Line> lines;
    private double latitude;
    private double longitude;
}