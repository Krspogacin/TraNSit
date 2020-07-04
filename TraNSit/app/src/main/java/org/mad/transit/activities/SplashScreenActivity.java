package org.mad.transit.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.mad.transit.MainActivity;
import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.database.DatabaseHelper;
import org.mad.transit.receiver.ConnectivityReceiver;
import org.mad.transit.repository.DepartureTimeRepository;
import org.mad.transit.repository.LineRepository;
import org.mad.transit.repository.TimetableRepository;
import org.mad.transit.sync.InitializeDatabaseService;
import org.mad.transit.sync.InitializeDatabaseTask;
import org.mad.transit.sync.SyncService;
import org.mad.transit.task.RetrieveTimetablesAsyncTask;
import org.mad.transit.util.NetworkUtil;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

public class SplashScreenActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String INITIALIZE_DB_FLAG = "initialize_db_flag";
    private static final String MONTH = "month";
    private static final String YEAR = "year";
    private ConnectivityReceiver connectivityReceiver = new ConnectivityReceiver();
    private boolean currentNetworkAvailability = true;
    private boolean initializeDBFlag;
    private TextView noInternetConnectionMessageView;
    private LinearLayout linearLayout;
    private TextView splashScreenMessage;
    private int currentMonth;
    private int currentYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ((TransitApplication) this.getApplicationContext()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_splash_screen);

        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        ConnectivityReceiver.Companion.setConnectivityReceiverListener(this);

        noInternetConnectionMessageView = findViewById(R.id.no_internet_connection_message_text_view);
        linearLayout = findViewById(R.id.linear_layout);

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

        splashScreenMessage = this.findViewById(R.id.splash_screen_message);

        Calendar calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);
        currentYear = calendar.get(Calendar.YEAR);

        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        initializeDBFlag = defaultSharedPreferences.getBoolean(INITIALIZE_DB_FLAG, false);
        boolean autoSyncEnabled = defaultSharedPreferences.getBoolean(this.getString(R.string.sync_preference_pref_key), false);
        int month = defaultSharedPreferences.getInt(MONTH, 5);
        final int year = defaultSharedPreferences.getInt(YEAR, 2020);

        initializeDBFlag = false;

        if (!initializeDBFlag) {
            if(NetworkUtil.isConnected(this)) {
                splashScreenMessage.setText(this.getString(R.string.loading_sync_message));
                Intent serviceIntent = new Intent(this, InitializeDatabaseService.class);
                serviceIntent.putExtra(MONTH, currentMonth);
                serviceIntent.putExtra(YEAR, currentYear);
                this.startService(serviceIntent);
            }else{
                noInternetConnectionMessageView.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.GONE);
                this.currentNetworkAvailability = false;
            }
        } else if (autoSyncEnabled && (month != currentMonth || year != currentYear)) {
            if(NetworkUtil.isConnected(this)) {
                splashScreenMessage.setText(this.getString(R.string.loading_sync_timetable_message));
                Intent serviceIntent = new Intent(this, SyncService.class);
                serviceIntent.putExtra(MONTH, currentMonth);
                serviceIntent.putExtra(YEAR, currentYear);
                this.startService(serviceIntent);
            }else{
                noInternetConnectionMessageView.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.GONE);
                this.currentNetworkAvailability = false;
            }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectivityReceiver.Companion.setConnectivityReceiverListener(null);
        unregisterReceiver(connectivityReceiver);
    }

    private void startMainActivity() {
        this.startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        this.finish();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (currentNetworkAvailability != isConnected) {
            if (isConnected) {
                linearLayout.setVisibility(View.VISIBLE);
                noInternetConnectionMessageView.setVisibility(View.GONE);
                if(!initializeDBFlag){
                    splashScreenMessage.setText(this.getString(R.string.loading_sync_message));
                    Intent serviceIntent = new Intent(this, InitializeDatabaseService.class);
                    serviceIntent.putExtra(MONTH, currentMonth);
                    serviceIntent.putExtra(YEAR, currentYear);
                    this.startService(serviceIntent);
                }else{
                    splashScreenMessage.setText(this.getString(R.string.loading_sync_timetable_message));
                    Intent serviceIntent = new Intent(this, SyncService.class);
                    serviceIntent.putExtra(MONTH, currentMonth);
                    serviceIntent.putExtra(YEAR, currentYear);
                    this.startService(serviceIntent);
                }
            } else {
                noInternetConnectionMessageView.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.GONE);
            }
            this.currentNetworkAvailability = isConnected;
        }
    }
}
