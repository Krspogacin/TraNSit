package org.mad.transit.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StartAction extends Action {

    @Override
    public SearchState execute(SearchState currentState) {
        return currentState;
    }

    @Override
    protected double timeCost() {
        return 0;
    }

    @Override
    protected double distance() {
        return 0;
    }

    @Override
    protected int velocity() {
        return 1;
    }
}
