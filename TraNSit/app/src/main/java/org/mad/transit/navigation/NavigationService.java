package org.mad.transit.navigation;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.mad.transit.MainActivity;
import org.mad.transit.R;
import org.mad.transit.activities.NavigationActivity;
import org.mad.transit.util.Constants;
import org.mad.transit.util.LocationsUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NavigationService extends Service {
    private static final String ACTION_STOP_SERVICE = "stop";
    private LocationRequest locationRequest;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private static final String TAG = "NavigationService";

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(getString(R.string.channel_id), getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(getString(R.string.channel_description));
            channel.enableLights(true);
            channel.setLightColor(R.color.colorPrimary);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        startForeground(1, this.createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            stopSelf();
        }

        runLocationUpdates();
        geofencingClient = LocationServices.getGeofencingClient(getBaseContext());
        geofenceHelper = new GeofenceHelper(getBaseContext());
        //TODO add Geofence locations
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void addGeofences(List<org.mad.transit.model.Location> locations) {
        List<Geofence> geofences = new ArrayList<>();
        for (org.mad.transit.model.Location location: locations) {
            Geofence geofence = geofenceHelper.getGeofence(location, Constants.GEOFENCE_NOTIFICATION_RADIUS, Geofence.GEOFENCE_TRANSITION_ENTER);
            geofences.add(geofence);
        }
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofences);

        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnFailureListener(e -> {
                    String errorMessage = geofenceHelper.getErrorString(e);
                    Log.e(TAG, errorMessage);
                });
    }

    private void runLocationUpdates() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getBaseContext());

        if (this.locationRequest == null) {
            this.locationRequest = LocationsUtil.createLocationRequest();
        }

        fusedLocationProviderClient.requestLocationUpdates(this.locationRequest, null, Looper.myLooper());
    }

    private Notification createNotification(){
        Intent intent = new Intent(this, NavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopSelf = new Intent(this, NavigationService.class);
        stopSelf.setAction(ACTION_STOP_SERVICE);
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, getString(R.string.channel_id))
                .setContentTitle(getText(R.string.navigation))
                .setContentText("U toku je proces navigacije...")
                .setColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_baseline_directions_bus_24)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .addAction(0, "Završi proces navigacije", pStopSelf)
                .build();

        return notification;
    }

    @Override
    public void onDestroy() {
        if (isForeground()) {
            Intent activityIntent = new Intent(this, MainActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(activityIntent);
        }
    }

    private boolean isForeground() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningTaskInfo = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningTaskInfo) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
