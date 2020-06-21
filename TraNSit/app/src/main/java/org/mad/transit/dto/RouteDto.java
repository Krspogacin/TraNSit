package org.mad.transit.dto;

import android.util.Pair;

import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RouteDto implements Serializable {
    private static final long serialVersionUID = -6192303553533873462L;
    private int totalDuration;
    private int totalPrice;
    private String nextDeparture;
    private List<ActionDto> actions;
    private Map<Pair<Long, LineDirection>, List<Location>> path;

    public List<ActionDto> getBusActions() {
        List<ActionDto> busActions = new ArrayList<>();
        for (ActionDto action : this.actions) {
            if (ActionType.BUS == action.getType()) {
                busActions.add(action);
            }
        }
        return busActions;
    }

    public Map<Pair<Long, LineDirection>, Pair<Location, Location>> getLineBoundaryLocations() {
        Map<Pair<Long, LineDirection>, Pair<Location, Location>> boundaryLocations = new LinkedHashMap<>();

        Map<Pair<Long, LineDirection>, List<ActionDto>> groupedActionsByLine = groupActionsByLine();
        for (Pair<Long, LineDirection> key : groupedActionsByLine.keySet()) {
            List<ActionDto> actionList = groupedActionsByLine.get(key);
            if (actionList != null && !actionList.isEmpty()) {
                Location startLocation = actionList.get(0).getStartLocation();
                Collections.reverse(actionList);
                Location endLocation = actionList.get(0).getEndLocation();
                boundaryLocations.put(key, new Pair<>(startLocation, endLocation));
            }
        }
        return boundaryLocations;
    }

    private Map<Pair<Long, LineDirection>, List<ActionDto>> groupActionsByLine() {
        Map<Pair<Long, LineDirection>, List<ActionDto>> map = new LinkedHashMap<>();
        for (ActionDto action : this.actions) {
            if (ActionType.BUS == action.getType()) {
                Pair<Long, LineDirection> key = new Pair<>(action.getLine().getId(), action.getLineDirection());
                if (!map.containsKey(key)) {
                    map.put(key, new ArrayList<>());
                }
                map.get(key).add(action);
            }
        }
        return map;
    }
}
