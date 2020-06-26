package org.mad.transit.navigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.mad.transit.R;
import org.mad.transit.activities.NavigationActivity;
import org.mad.transit.fragments.NavigationMapFragment;
import org.mad.transit.model.Location;

import java.util.List;

public class GeofenceBrodcastReciever extends BroadcastReceiver {
    private static final String TAG = "GeofenceBrodcastRecieve";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()){
            Log.e(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        int transitionType = geofencingEvent.getGeofenceTransition();

        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            notificationHelper.sendHighPriorityNotification(context.getString(R.string.navigation), context.getString(R.string.notification_content), NavigationActivity.class);
        }
    }
}
