package org.mad.transit.navigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.GeofencingEvent;

import org.mad.transit.R;

public class GeofenceNotificationBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceNotificationBR";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        notificationHelper.sendHighPriorityNotification(context.getString(R.string.navigation), context.getString(R.string.notification_content));
    }
}
