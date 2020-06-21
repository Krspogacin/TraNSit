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
        // only 1 walk solution should be taken into account
        return !isAlsoWalkSolution(potentialSolution) && isUsingDifferentLines(potentialSolution);
    }

    private boolean isAlsoWalkSolution(Solution potentialSolution) {
        return usedLines.isEmpty() && potentialSolution.getUsedLines().isEmpty();
    }

    private boolean isUsingDifferentLines(Solution potentialSolution) {
        return !Sets.difference(usedLines, potentialSolution.getUsedLines()).isEmpty() || !Sets.difference(potentialSolution.getUsedLines(), usedLines).isEmpty();
    }

    public RouteDto convertToRoute() {
        SearchState lastState = this.actions.get(this.actions.size() - 1).second;
        RouteDto.RouteDtoBuilder routeBuilder = RouteDto.builder()
                .totalDuration((int) Math.ceil(lastState.getTimeElapsed() * 60))
                .totalPrice(lastState.getTravelCost());
        List<ActionDto> routeActions = new ArrayList<>();
        for (int i = 1; i < this.actions.size(); i++) {
            Pair<Action, SearchState> previousPair = this.actions.get(i - 1);
            Pair<Action, SearchState> currentPair = this.actions.get(i);
            Action action = currentPair.first;
            SearchState state = currentPair.second;
            ActionDto.ActionDtoBuilder actionBuilder = ActionDto.builder()
                    .duration((int) Math.ceil((state.getTimeElapsed() - previousPair.second.getTimeElapsed()) * 60))
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
