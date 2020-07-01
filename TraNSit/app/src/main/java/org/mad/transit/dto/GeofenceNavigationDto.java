package org.mad.transit.dto;

import org.mad.transit.model.Stop;

import java.io.Serializable;

public class GeofenceNavigationDto implements Serializable {

    private static final long serialVersionUID = 8789036907195883650L;
    private final Stop stop;
    private final ActionType actionType;
    private final int position;
    private final String geofenceRequestId;

    public GeofenceNavigationDto(Stop stop, ActionType actionType, int position, String geofenceRequestId) {
        this.stop = stop;
        this.actionType = actionType;
        this.position = position;
        this.geofenceRequestId = geofenceRequestId;
    }

    public Stop getStop() {
        return this.stop;
    }

    public ActionType getActionType() {
        return this.actionType;
    }

    public int getPosition() {
        return this.position;
    }

    public String getGeofenceRequestId() {
        return this.geofenceRequestId;
    }
}