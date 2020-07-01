package org.mad.transit.navigation;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import org.mad.transit.R;
import org.mad.transit.activities.NavigationActivity;
import org.mad.transit.dto.NavigationDto;
import org.mad.transit.dto.RouteDto;
import org.mad.transit.model.Location;

import java.util.Random;

import lombok.Setter;

public class NotificationHelper extends ContextWrapper {

    @Setter
    private RouteDto route;

    @Setter
    private Location startLocation;

    @Setter
    private Location endLocation;

    @Setter
    private NavigationDto navigationDto;

    public NotificationHelper(Context base) {
        super(base);
    }

    public void sendHighPriorityNotification(String title, String body, Class activityName) {
        Notification notification = new NotificationCompat.Builder(this, this.getString(R.string.channel_id))
                .setContentTitle(title)
                .setContentText(body)
                .setColor(ContextCompat.getColor(this.getBaseContext(), R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_baseline_directions_bus_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(this).notify(new Random().nextInt(), notification);
    }

    public Notification createNotClosableHighPriorityNotification(String title, String body, String actionName, PendingIntent action, Class activityName) {
        return new NotificationCompat.Builder(this, this.getString(R.string.channel_id))
                .setContentTitle(title)
                .setContentText(body)
                .setColor(ContextCompat.getColor(this.getBaseContext(), R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_baseline_directions_bus_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(this.createContentIntent(activityName))
                .setOnlyAlertOnce(true)
                .addAction(0, actionName, action)
                .build();
    }

    public PendingIntent createContentIntent(Class activityName) {
        Intent intent = new Intent(this, activityName);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        intent.putExtra(NavigationActivity.ROUTE, this.route);
        intent.putExtra(NavigationActivity.START_LOCATION, this.startLocation);
        intent.putExtra(NavigationActivity.END_LOCATION, this.endLocation);
        intent.putExtra(NavigationActivity.NAVIGATION_DTO, this.navigationDto);

        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
