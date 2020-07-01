package org.mad.transit.navigation;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;

import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.mad.transit.MainActivity;
import org.mad.transit.R;
import org.mad.transit.activities.NavigationActivity;
import org.mad.transit.dto.ActionType;
import org.mad.transit.dto.GeofenceNavigationDto;
import org.mad.transit.dto.NavigationDto;
import org.mad.transit.dto.RouteDto;
import org.mad.transit.model.Location;
import org.mad.transit.model.NavigationStop;
import org.mad.transit.util.Constants;
import org.mad.transit.util.LocationsUtil;
import org.mad.transit.util.ParcelableUtil;
import org.mad.transit.util.SerializeUtil;

import java.util.ArrayList;
import java.util.List;

public class NavigationService extends Service {

    public static final String END_LOCATIONS = "end_locations";
    private static final String ACTION_STOP_SERVICE = "stop";
    public static final String SERVICE_RESULT_RECEIVER = "service_result_receiver";
    private LocationRequest locationRequest;
    private GeofenceHelper geofenceHelper;
    private NotificationHelper notificationHelper;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private PendingIntent pendingIntent;
    private List<Location> endLocations;
    private RouteDto route;
    private Location startLocation;
    private Location endLocation;
    private PendingIntent navigationGeofencePendingIntent;
    private NavigationDto navigationDto;
    private Notification notification;
    private ArrayList<GeofenceNavigationDto> geofenceNavigationDtoList;
    private List<Geofence> geofences;
    private SharedPreferences defaultSharedPreferences;

    private final ResultReceiver serviceResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            GeofenceNavigationDto geofenceNavigationDto = (GeofenceNavigationDto) resultData.getSerializable(NavigationActivity.GEOFENCE_NAVIGATION_DTO);

            if (geofenceNavigationDto.getActionType() == ActionType.BUS) {
                int size = NavigationService.this.navigationDto.getNavigationStops().get(geofenceNavigationDto.getPosition()).size();
                for (NavigationStop stop : NavigationService.this.navigationDto.getNavigationStops().get(geofenceNavigationDto.getPosition())) {
                    if (stop.equals((NavigationStop) geofenceNavigationDto.getStop())) {
                        stop.setPassed(true);
                        if (stop.equals(NavigationService.this.navigationDto.getNavigationStops().get(geofenceNavigationDto.getPosition()).get(size - 1))) {
                            NavigationService.this.navigationDto.setCardPosition(NavigationService.this.navigationDto.getCardPosition() + 1);
                        }
                        break;
                    }
                }
            } else {
                NavigationService.this.navigationDto.setCardPosition(NavigationService.this.navigationDto.getCardPosition() + 1);
            }

            NavigationService.this.notificationHelper.setNavigationDto(NavigationService.this.navigationDto);
            NavigationService.this.notification.contentIntent = NavigationService.this.notificationHelper.createContentIntent(NavigationActivity.class);
            NavigationService.this.startForeground(1, NavigationService.this.notification);
        }
    };

    @Override
    public void onCreate() {
        this.geofenceHelper = new GeofenceHelper(this.getBaseContext());
        this.notificationHelper = new NotificationHelper(this.getBaseContext());
        this.defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            this.stopSelf();
            return Service.START_NOT_STICKY;
        }

        Bundle bundle = intent.getBundleExtra(NavigationActivity.DATA_BUNDLE);

        if (this.navigationGeofencePendingIntent != null) {
            this.geofenceHelper.removeGeofences(this.navigationGeofencePendingIntent);
            bundle.putSerializable(NavigationActivity.GEOFENCE_NAVIGATION_DTO_LIST, this.geofenceNavigationDtoList);
        } else {
            this.geofenceNavigationDtoList = (ArrayList<GeofenceNavigationDto>) bundle.getSerializable(NavigationActivity.GEOFENCE_NAVIGATION_DTO_LIST);

            if (this.geofenceNavigationDtoList == null) {
                return Service.START_NOT_STICKY;
            }

            this.geofences = new ArrayList<>();
            for (GeofenceNavigationDto geofenceNavigationDto : this.geofenceNavigationDtoList) {
                if (geofenceNavigationDto.getStop() != null) {
                    Geofence geofence = this.geofenceHelper.getGeofence(geofenceNavigationDto.getStop().getLocation(),
                            Constants.GEOFENCE_NAVIGATION_RADIUS,
                            Geofence.GEOFENCE_TRANSITION_ENTER,
                            geofenceNavigationDto.getGeofenceRequestId());
                    this.geofences.add(geofence);
                }
            }
        }

        this.runLocationUpdates();

        if (this.geofences != null && !this.geofences.isEmpty()) {
            Intent geofenceNavigationBroadcastReceiverIntent = new Intent(this, GeofenceNavigationBroadcastReceiver.class);
            bundle.putParcelable(SERVICE_RESULT_RECEIVER, this.serviceResultReceiver);
            geofenceNavigationBroadcastReceiverIntent.putExtra(NavigationActivity.DATA_BUNDLE, bundle);
            this.navigationGeofencePendingIntent = PendingIntent.getBroadcast(this, 1, geofenceNavigationBroadcastReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            this.geofenceHelper.addGeofences(this.geofences, this.navigationGeofencePendingIntent);
        }

        if (this.endLocations == null || this.endLocations.isEmpty()) {
            this.endLocations = (List<Location>) intent.getSerializableExtra(END_LOCATIONS);
            this.route = intent.getParcelableExtra(NavigationActivity.ROUTE);
            this.startLocation = (Location) intent.getSerializableExtra(NavigationActivity.START_LOCATION);
            this.endLocation = (Location) intent.getSerializableExtra(NavigationActivity.END_LOCATION);

            this.navigationDto = bundle.getParcelable(NavigationActivity.NAVIGATION_DTO);
            bundle.remove(NavigationActivity.NAVIGATION_DTO);

            this.notificationHelper.setRoute(this.route);
            this.notificationHelper.setStartLocation(this.startLocation);
            this.notificationHelper.setEndLocation(this.endLocation);
            this.notificationHelper.setNavigationDto(this.navigationDto);

            boolean navigationNotification = this.defaultSharedPreferences.getBoolean(this.getString(R.string.navigation_notification_pref_key), false);

            if (navigationNotification && !this.endLocations.isEmpty()) {
                this.geofenceHelper.addGeofences(this.endLocations, Constants.GEOFENCE_NOTIFICATION_RADIUS, this.getPendingIntent());
            }

            Intent stopSelf = new Intent(this, NavigationService.class);
            stopSelf.setAction(NavigationService.ACTION_STOP_SERVICE);
            PendingIntent stopSelfPendingIntent = PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT);

            this.notification = this.notificationHelper.createNotClosableHighPriorityNotification(this.getString(R.string.navigation),
                    this.getString(R.string.navigation_main_notification_content_text),
                    this.getString(R.string.navigation_main_notification_action_text),
                    stopSelfPendingIntent,
                    NavigationActivity.class);

            this.startForeground(1, this.notification);
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void runLocationUpdates() {
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getBaseContext());

        if (this.locationRequest == null) {
            this.locationRequest = LocationsUtil.createLocationRequest();
        }

        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
            }
        };
        this.fusedLocationProviderClient.requestLocationUpdates(this.locationRequest, this.locationCallback, Looper.myLooper());
    }

    @Override
    public void onDestroy() {
        boolean serviceActive = this.defaultSharedPreferences.getBoolean(this.getString(R.string.service_active_pref_key), true);
        if (this.isForeground() && serviceActive) {
            Intent activityIntent = new Intent(this, MainActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.startActivity(activityIntent);
        }

        if (this.fusedLocationProviderClient != null && this.locationCallback != null) {
            this.fusedLocationProviderClient.removeLocationUpdates(this.locationCallback);
        }

        this.geofenceHelper.removeGeofences(this.getPendingIntent());

        if (this.navigationGeofencePendingIntent != null) {
            this.geofenceHelper.removeGeofences(this.navigationGeofencePendingIntent);
        }

        this.defaultSharedPreferences.edit().putBoolean(this.getString(R.string.service_active_pref_key), false).apply();
    }

    public PendingIntent getPendingIntent() {
        if (this.pendingIntent != null) {
            return this.pendingIntent;
        }
        Intent intent = new Intent(this, GeofenceNotificationBroadcastReceiver.class);
        intent.putExtra(NavigationActivity.ROUTE, ParcelableUtil.marshall(this.route));
        intent.putExtra(NavigationActivity.START_LOCATION, SerializeUtil.convertToBytes(this.startLocation));
        intent.putExtra(NavigationActivity.END_LOCATION, SerializeUtil.convertToBytes(this.endLocation));
        this.pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return this.pendingIntent;
    }

    private boolean isForeground() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningTaskInfo = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningTaskInfo) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(this.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
