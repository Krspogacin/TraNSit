package org.mad.transit.sync;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import org.mad.transit.MainActivity;
import org.mad.transit.TransitApplication;
import org.mad.transit.repository.DepartureTimeRepository;
import org.mad.transit.repository.LineRepository;
import org.mad.transit.repository.TimetableRepository;
import org.mad.transit.task.RetrieveTimetablesAsyncTask;

import javax.inject.Inject;

import androidx.preference.PreferenceManager;

public class SyncService extends Service {
    private static final String MONTH = "month";
    private static final String YEAR = "year";

    @Inject
    TimetableRepository timetableRepository;

    @Inject
    DepartureTimeRepository departureTimeRepository;

    @Inject
    LineRepository lineRepository;

    @Override
    public void onCreate() {

        ((TransitApplication) this.getApplicationContext()).getAppComponent().inject(this);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        int currentMonth = intent.getIntExtra(MONTH, 5);
        int currentYear = intent.getIntExtra(YEAR, 2020);
        new RetrieveTimetablesAsyncTask(this.getContentResolver(),
                this.timetableRepository,
                this.lineRepository,
                this.departureTimeRepository,
                () -> {
                    defaultSharedPreferences.edit().putInt(MONTH, currentMonth).apply();
                    defaultSharedPreferences.edit().putInt(YEAR, currentYear).apply();
                    startMainActivity();
                }).execute();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startMainActivity() {
        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(activityIntent);
    }
}
