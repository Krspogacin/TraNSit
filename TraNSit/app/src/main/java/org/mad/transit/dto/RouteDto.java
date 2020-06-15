package org.mad.transit.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RouteDto implements Serializable {
    private static final long serialVersionUID = -6192303553533873462L;
    private int totalDuration;
    private double totalPrice;
    private List<ActionDto> actions;
}
