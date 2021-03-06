package org.mad.transit.search;

import org.mad.transit.model.Line;
import org.mad.transit.model.LineDirection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class BusAction extends Action {
    private Line line;
    private LineDirection lineDirection;

    @Override
    public SearchState execute(SearchState currentState) {
        SearchState nextState = super.execute(currentState);
        nextState.setLine(this.line);
        nextState.setLineDirection(this.lineDirection);
        return nextState;
    }

    @Override
    protected int velocity() {
        return 40;
    }
}
