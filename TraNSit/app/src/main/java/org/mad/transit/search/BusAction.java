package org.mad.transit.search;

import org.mad.transit.model.Line;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class BusAction extends Action {
    private Line line;
    private double price;

    @Override
    public SearchState execute(SearchState currentState) {
        SearchState nextState = super.execute(currentState);

        // TODO calculate wait time
        double waitTime = 0;

        nextState.setTimeElapsed(nextState.getTimeElapsed() + waitTime);
        nextState.setLine(this.line);
        return nextState;
    }

    @Override
    protected int velocity() {
        return 40;
    }
}
