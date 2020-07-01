package org.mad.transit.navigation;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import org.mad.transit.model.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GeofenceHelper extends ContextWrapper {

    private static final String TAG = "GeofenceHelper";
    private final GeofencingClient geofencingClient;

    public GeofenceHelper(Context base) {
        super(base);
        this.geofencingClient = LocationServices.getGeofencingClient(base);
    }

    public GeofencingRequest getGeofencingRequest(List<Geofence> geofences) {
        return new GeofencingRequest.Builder()
                .addGeofences(geofences)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    public Geofence getGeofence(Location location, float radius, int transitionType, String requestId) {
        return new Geofence.Builder()
                .setCircularRegion(location.getLatitude(), location.getLongitude(), radius)
                .setRequestId(requestId)
                .setTransitionTypes(transitionType)
                .setLoiteringDelay(5000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    public void addGeofences(List<Location> locations, float radius, PendingIntent pendingIntent) {
        List<Geofence> geofences = new ArrayList<>();
        for (Location location : locations) {
            Geofence geofence = this.getGeofence(location, radius, Geofence.GEOFENCE_TRANSITION_ENTER, UUID.randomUUID().toString());
            geofences.add(geofence);
        }
        this.addGeofences(geofences, pendingIntent);
    }

    public void addGeofences(List<Geofence> geofences, PendingIntent pendingIntent) {
        GeofencingRequest geofencingRequest = this.getGeofencingRequest(geofences);
        this.geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnFailureListener(e -> {
                    String errorMessage = this.getErrorString(e);
                    Log.e(TAG, errorMessage);
                });
    }

    public void removeGeofences(PendingIntent pendingIntent) {
        this.geofencingClient.removeGeofences(pendingIntent);
    }

    public void removeGeofences(List<String> geofenceRequestIds) {
        this.geofencingClient.removeGeofences(geofenceRequestIds);
    }

    public String getErrorString(Exception e) {
        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            switch (apiException.getStatusCode()) {
                case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                    return "GEOFENCE_NOT_AVAILABLE";
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                    return "GEOFENCE_TOO_MANY_GEOFENCES";
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "GEOFENCE_TOO_MANY_PENDING_INTENTS";
            }
        }
        return e.getLocalizedMessage();
    }
}
