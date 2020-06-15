package org.mad.transit.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionDto {
    private ActionType type;
    private LocationDto startLocation;
    private LocationDto endLocation;
    private List<LocationDto> path;
    private StopDto stop;
    private LineDto line;
    private int duration;
}
