package org.mad.transit.navigation;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;

import org.mad.transit.model.Location;

import java.util.List;
import java.util.Random;

public class GeofenceHelper extends ContextWrapper {
    private PendingIntent pendingIntent;

    public GeofenceHelper(Context base) {
        super(base);
    }

    public GeofencingRequest getGeofencingRequest(List<Geofence> geofences){
        return new GeofencingRequest.Builder()
                .addGeofences(geofences)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    public Geofence getGeofence(Location location, float radius, int transitionTypes){
        return new Geofence.Builder()
                .setCircularRegion(location.getLatitude(), location.getLongitude(), radius)
                .setRequestId("" + new Random().nextInt())
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(5000) //only for dwell
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
        }

    public PendingIntent getPendingIntent(){
        if (pendingIntent != null){
            return pendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBrodcastReciever.class);
        pendingIntent = PendingIntent.getBroadcast(this,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public String getErrorString(Exception e){
        if (e instanceof ApiException){
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
