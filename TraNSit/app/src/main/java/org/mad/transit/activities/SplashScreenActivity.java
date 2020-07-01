package org.mad.transit.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.mad.transit.MainActivity;
import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.repository.DepartureTimeRepository;
import org.mad.transit.repository.LineRepository;
import org.mad.transit.repository.TimetableRepository;
import org.mad.transit.sync.InitializeDatabaseTask;
import org.mad.transit.task.RetrieveTimetablesAsyncTask;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String INITIALIZE_DB_FLAG = "initialize_db_flag";
    private static final String MONTH = "month";
    private static final String YEAR = "year";

    @Inject
    TimetableRepository timetableRepository;

    @Inject
    DepartureTimeRepository departureTimeRepository;

    @Inject
    LineRepository lineRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ((TransitApplication) this.getApplicationContext()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_splash_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(this.getString(R.string.channel_id), this.getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(this.getString(R.string.channel_description));
            channel.enableLights(true);
            channel.setLightColor(R.color.colorPrimary);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                channel.setAllowBubbles(true);
//            }
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        TextView splashScreenMessage = this.findViewById(R.id.splash_screen_message);

        Calendar calendar = Calendar.getInstance();
        final int currentMonth = calendar.get(Calendar.MONTH);
        final int currentYear = calendar.get(Calendar.YEAR);

        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean initializeDBFlag = defaultSharedPreferences.getBoolean(INITIALIZE_DB_FLAG, false);
        boolean autoSyncEnabled = defaultSharedPreferences.getBoolean(this.getString(R.string.sync_preference_pref_key), false);
        final int month = defaultSharedPreferences.getInt(MONTH, 5);
        final int year = defaultSharedPreferences.getInt(YEAR, 2020);
        if (!initializeDBFlag) {
            splashScreenMessage.setText(this.getString(R.string.loading_sync_message));
            //INIT DATABASE AND START MAIN ACTIVITY AFTER FINISHED
            new InitializeDatabaseTask(this.getContentResolver(), () -> {
                defaultSharedPreferences.edit().putBoolean(SplashScreenActivity.INITIALIZE_DB_FLAG, true).apply();
                defaultSharedPreferences.edit().putInt(SplashScreenActivity.MONTH, currentMonth).apply();
                defaultSharedPreferences.edit().putInt(SplashScreenActivity.YEAR, currentYear).apply();
                SplashScreenActivity.this.startMainActivity();
            }).execute();
        } else if (autoSyncEnabled && (month != currentMonth || year != currentYear)) {
            splashScreenMessage.setText(this.getString(R.string.loading_sync_timetable_message));
            RetrieveTimetablesAsyncTask retrieveTimetablesAsyncTask = new RetrieveTimetablesAsyncTask(this.getContentResolver(),
                    this.timetableRepository,
                    this.lineRepository,
                    this.departureTimeRepository,
                    () -> {
                        defaultSharedPreferences.edit().putInt(SplashScreenActivity.MONTH, currentMonth).apply();
                        defaultSharedPreferences.edit().putInt(SplashScreenActivity.YEAR, currentYear).apply();
                        SplashScreenActivity.this.startMainActivity();
                    });
            retrieveTimetablesAsyncTask.execute();
        } else {
            splashScreenMessage.setText(this.getString(R.string.loading_message));
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    SplashScreenActivity.this.startMainActivity();
                }
            }, 500);
        }
    }

    private void startMainActivity() {
        this.startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        this.finish();
    }
}
