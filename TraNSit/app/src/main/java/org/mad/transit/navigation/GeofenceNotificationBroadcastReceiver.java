package org.mad.transit.navigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.mad.transit.R;
import org.mad.transit.activities.NavigationActivity;
import org.mad.transit.dto.RouteDto;
import org.mad.transit.model.Location;
import org.mad.transit.util.ParcelableUtil;
import org.mad.transit.util.SerializeUtil;

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

        int transitionType = geofencingEvent.getGeofenceTransition();

        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {

            RouteDto route = ParcelableUtil.unmarshall(intent.getByteArrayExtra(NavigationActivity.ROUTE), RouteDto.CREATOR);
            Location startLocation = SerializeUtil.convertFromBytes(intent.getByteArrayExtra(NavigationActivity.START_LOCATION));
            Location endLocation = SerializeUtil.convertFromBytes(intent.getByteArrayExtra(NavigationActivity.END_LOCATION));

            notificationHelper.setRoute(route);
            notificationHelper.setStartLocation(startLocation);
            notificationHelper.setEndLocation(endLocation);

            notificationHelper.sendHighPriorityNotification(context.getString(R.string.navigation),
                    context.getString(R.string.notification_content),
                    NavigationActivity.class);
        }
    }
}
