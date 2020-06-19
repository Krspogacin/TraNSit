package org.mad.transit.search;

import android.util.Pair;

import com.google.common.collect.Sets;

import org.mad.transit.dto.ActionDto;
import org.mad.transit.dto.ActionType;
import org.mad.transit.dto.RouteDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class Solution {
    private List<Pair<Action, SearchState>> actions;
    private Set<Long> usedLines;

    public Solution(List<Pair<Action, SearchState>> actions) {
        this.actions = actions;
        this.usedLines = new HashSet<>();
        for (Pair<Action, SearchState> pair : actions) {
            if (pair.first instanceof BusAction) {
                BusAction action = (BusAction) pair.first;
                usedLines.add(action.getLine().getId());
            }
        }
    }

    public boolean isDifferent(Solution potentialSolution) {
        return usedLines.isEmpty() ||
                Sets.difference(usedLines, potentialSolution.getUsedLines()).size() != 0 ||
                Sets.difference(potentialSolution.getUsedLines(), usedLines).size() != 0;
    }

    public RouteDto convertToRoute() {
        RouteDto.RouteDtoBuilder routeBuilder = RouteDto.builder();
        routeBuilder.totalDuration((int) (this.actions.get(this.actions.size() - 1).second.getTimeElapsed() * 60));
        List<ActionDto> routeActions = new ArrayList<>();
        for (int i = 1; i < this.actions.size(); i++) {
            Pair<Action, SearchState> previousPair = this.actions.get(i - 1);
            Pair<Action, SearchState> currentPair = this.actions.get(i);
            Action action = currentPair.first;
            SearchState state = currentPair.second;
            ActionDto.ActionDtoBuilder actionBuilder = ActionDto.builder()
                    .duration((int) ((state.getTimeElapsed() - previousPair.second.getTimeElapsed()) * 60))
                    .startLocation(action.getStartLocation())
                    .endLocation(action.getEndLocation())
                    .stop(state.getStop());
            if (action instanceof BusAction) {
                actionBuilder
                        .type(ActionType.BUS)
                        .line(state.getLine())
                        .lineDirection(state.getLineDirection());
            } else {
                actionBuilder.type(ActionType.WALK);
            }
            routeActions.add(actionBuilder.build());
        }
        return routeBuilder
                .actions(routeActions)
                .build();
    }
}
