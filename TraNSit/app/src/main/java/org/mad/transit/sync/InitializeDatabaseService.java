package org.mad.transit.sync;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import org.mad.transit.MainActivity;
import org.mad.transit.database.DatabaseHelper;

import androidx.preference.PreferenceManager;

public class InitializeDatabaseService extends Service{
    private static final String INITIALIZE_DB_FLAG = "initialize_db_flag";
    private static final String MONTH = "month";
    private static final String YEAR = "year";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        databaseHelper.onUpgrade(databaseHelper.getWritableDatabase(), 1, 2);
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        int currentMonth = intent.getIntExtra(MONTH, 5);
        int currentYear = intent.getIntExtra(YEAR, 2020);
            new InitializeDatabaseTask(this.getContentResolver(), () -> {
                defaultSharedPreferences.edit().putBoolean(InitializeDatabaseService.INITIALIZE_DB_FLAG, true).apply();
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
