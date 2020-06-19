package org.mad.transit.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class WalkAction extends Action {
    @Override
    protected int velocity() {
        return 4;
    }
}
