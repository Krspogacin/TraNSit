package org.mad.transit.dto;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RouteDto implements Parcelable {
    private static final long serialVersionUID = -6192303553533873462L;
    private int totalDuration;
    private int totalPrice;
    private String nextDeparture;
    private List<ActionDto> actions;
    private Map<Pair<Long, LineDirection>, List<Location>> path;

    protected RouteDto(Parcel in) {
        this.totalDuration = in.readInt();
        this.totalPrice = in.readInt();
        this.nextDeparture = in.readString();
        this.actions = new ArrayList<>();
        in.readList(this.actions, ActionDto.class.getClassLoader());

        int size = in.readInt();
        this.path = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Long lineKey = in.readLong();
            LineDirection lineDirection = LineDirection.valueOf(in.readString());
            List<Location> locations = new ArrayList<>();
            in.readList(locations, Location.class.getClassLoader());
            this.path.put(new Pair<>(lineKey, lineDirection), locations);
        }
    }

    public RouteDto(int totalDuration, int totalPrice, String nextDeparture, List<ActionDto> actions, Map<Pair<Long, LineDirection>, List<Location>> path) {
        this.totalDuration = totalDuration;
        this.totalPrice = totalPrice;
        this.nextDeparture = nextDeparture;
        this.actions = actions;
        this.path = path;
    }

    public static final Creator<RouteDto> CREATOR = new Creator<RouteDto>() {
        @Override
        public RouteDto createFromParcel(Parcel in) {
            return new RouteDto(in);
        }

        @Override
        public RouteDto[] newArray(int size) {
            return new RouteDto[size];
        }
    };

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

        Map<Pair<Long, LineDirection>, List<ActionDto>> groupedActionsByLine = this.groupActionsByLine();
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

    public Map<Pair<Long, LineDirection>, List<ActionDto>> groupActionsByLine() {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.totalDuration);
        dest.writeInt(this.totalPrice);
        dest.writeString(this.nextDeparture);
        dest.writeList(this.actions);
        dest.writeInt(this.path.size());
        for (Map.Entry<Pair<Long, LineDirection>, List<Location>> entry : this.path.entrySet()) {
            dest.writeLong(entry.getKey().first);
            dest.writeString(entry.getKey().second.toString());
            dest.writeList(entry.getValue());
        }
    }

    public List<ActionDto> getActions() {
        return this.actions;
    }
}
