package org.mad.transit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class NavigationStop extends Stop {
    private static final long serialVersionUID = -1540331636976984635L;
    private boolean passed;
    private int minutes;

    public NavigationStop(Stop stop, boolean passed, int minutes) {
        this.title = stop.title;
        this.location = stop.location;
        this.lines = stop.lines;
        this.passed = passed;
        this.minutes = minutes;
    }
}