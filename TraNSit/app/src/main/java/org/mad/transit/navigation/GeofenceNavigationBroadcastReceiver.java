package org.mad.transit.navigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.mad.transit.activities.NavigationActivity;
import org.mad.transit.dto.GeofenceNavigationDto;

import java.util.List;

public class GeofenceNavigationBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceNavigationBR";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                Log.e(TAG, "onReceive: Error receiving geofence event...");
                return;
            }

            Geofence geofence = geofencingEvent.getTriggeringGeofences().get(0);

            Bundle bundle = intent.getBundleExtra(NavigationActivity.DATA_BUNDLE);
            if (bundle != null) {
                ResultReceiver resultReceiver = bundle.getParcelable(NavigationActivity.RECEIVER);
                ResultReceiver serviceResultReceiver = bundle.getParcelable(NavigationService.SERVICE_RESULT_RECEIVER);
                bundle.remove(NavigationActivity.RECEIVER);

                List<GeofenceNavigationDto> geofenceNavigationDtoList = (List<GeofenceNavigationDto>) bundle.getSerializable(NavigationActivity.GEOFENCE_NAVIGATION_DTO_LIST);

                if (geofenceNavigationDtoList == null || resultReceiver == null || serviceResultReceiver == null) {
                    return;
                }

                for (GeofenceNavigationDto geofenceNavigationDto : geofenceNavigationDtoList) {
                    if (geofenceNavigationDto.getGeofenceRequestId().equals(geofence.getRequestId())) {
                        bundle.putSerializable(NavigationActivity.GEOFENCE_NAVIGATION_DTO, geofenceNavigationDto);
                        resultReceiver.send(1234, bundle);
                        serviceResultReceiver.send(4321, bundle);
                        break;
                    }
                }
            }
        }
    }
}
