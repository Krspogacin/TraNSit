package org.mad.transit.search;

import android.util.Pair;

import com.google.common.collect.Sets;

import org.mad.transit.dto.ActionDto;
import org.mad.transit.dto.ActionType;
import org.mad.transit.dto.LineDto;
import org.mad.transit.dto.LocationDto;
import org.mad.transit.dto.RouteDto;
import org.mad.transit.dto.StopDto;
import org.mad.transit.model.Line;
import org.mad.transit.model.Stop;

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
        for (int i = 1; i < routeActions.size(); i++) {
            Pair<Action, SearchState> previousPair = this.actions.get(i - 1);
            Pair<Action, SearchState> currentPair = this.actions.get(i);
            Action action = currentPair.first;
            SearchState state = currentPair.second;
            ActionDto.ActionDtoBuilder actionBuilder = ActionDto.builder()
                    .duration((int) ((state.getTimeElapsed() - previousPair.second.getTimeElapsed()) * 60))
                    .startLocation(new LocationDto(action.getStartLocation().getLatitude().toString(), action.getStartLocation().getLongitude().toString()))
                    .endLocation(new LocationDto(action.getEndLocation().getLatitude().toString(), action.getEndLocation().getLongitude().toString()));
            if (action instanceof BusAction) {
                Line line = state.getLine();
                actionBuilder
                        .type(ActionType.BUS)
                        .line(new LineDto(line.getTitle(), line.getNumber(), line.getType().name(), null, null));
            } else {
                actionBuilder.type(ActionType.WALK);
            }
            Stop stop = state.getStop();
            if (stop != null) {
                actionBuilder.stop(new StopDto(stop.getLocation().getLatitude().toString(), stop.getLocation().getLongitude().toString(), stop.getTitle(), null));
            }
            routeActions.add(actionBuilder.build());
        }
        return routeBuilder
                .actions(routeActions)
                .build();
    }
}
