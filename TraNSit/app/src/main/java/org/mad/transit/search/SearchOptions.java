package org.mad.transit.search;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchOptions implements Serializable {
    private static final long serialVersionUID = 4501943647222730448L;
    private int solutionCount;
    private boolean transfersEnabled;
    private int hours;
    private int minutes;
}
