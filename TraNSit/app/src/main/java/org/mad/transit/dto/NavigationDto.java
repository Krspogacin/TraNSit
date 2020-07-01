package org.mad.transit.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.mad.transit.model.NavigationStop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigationDto implements Parcelable {
    private int cardPosition;
    private final ArrayList<List<ActionDto>> routeParts;
    private final HashMap<Integer, List<NavigationStop>> navigationStops;

    public NavigationDto(int cardPosition, ArrayList<List<ActionDto>> routeParts, HashMap<Integer, List<NavigationStop>> navigationStops) {
        this.cardPosition = cardPosition;
        this.routeParts = routeParts;
        this.navigationStops = navigationStops;
    }

    protected NavigationDto(Parcel in) {
        this.cardPosition = in.readInt();

        int routePartsSize = in.readInt();
        this.routeParts = new ArrayList<>();
        for (int i = 0; i < routePartsSize; i++) {
            List<ActionDto> actions = new ArrayList<>();
            in.readList(actions, ActionDto.class.getClassLoader());
            this.routeParts.add(actions);
        }

        int navigationStopsSize = in.readInt();
        this.navigationStops = new HashMap<>();
        for (int i = 0; i < navigationStopsSize; i++) {
            int key = in.readInt();
            ArrayList<NavigationStop> value = new ArrayList<>();
            in.readList(value, NavigationStop.class.getClassLoader());
            this.navigationStops.put(key, value);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.cardPosition);

        dest.writeInt(this.routeParts.size());
        for (List<ActionDto> actions : this.routeParts) {
            dest.writeList(actions);
        }

        dest.writeInt(this.navigationStops.size());
        for (Map.Entry<Integer, List<NavigationStop>> entry : this.navigationStops.entrySet()) {
            dest.writeInt(entry.getKey());
            dest.writeList(entry.getValue());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NavigationDto> CREATOR = new Creator<NavigationDto>() {
        @Override
        public NavigationDto createFromParcel(Parcel in) {
            return new NavigationDto(in);
        }

        @Override
        public NavigationDto[] newArray(int size) {
            return new NavigationDto[size];
        }
    };

    public void setCardPosition(int cardPosition) {
        this.cardPosition = cardPosition;
    }

    public int getCardPosition() {
        return this.cardPosition;
    }

    public ArrayList<List<ActionDto>> getRouteParts() {
        return this.routeParts;
    }

    public HashMap<Integer, List<NavigationStop>> getNavigationStops() {
        return this.navigationStops;
    }
}