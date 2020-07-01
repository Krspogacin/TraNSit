package org.mad.transit.dto;

import org.mad.transit.model.Line;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionDto implements Serializable {
    private static final long serialVersionUID = 649624443771071580L;
    private ActionType type;
    private Location startLocation;
    private Location endLocation;
    private Stop stop;
    private Line line;
    private LineDirection lineDirection;
    private int duration;

    public ActionType getType() {
        return this.type;
    }

    public Location getStartLocation() {
        return this.startLocation;
    }

    public Location getEndLocation() {
        return this.endLocation;
    }

    public Stop getStop() {
        return this.stop;
    }

    public Line getLine() {
        return this.line;
    }

    public LineDirection getLineDirection() {
        return this.lineDirection;
    }

    public int getDuration() {
        return this.duration;
    }
}
