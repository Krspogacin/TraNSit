package org.mad.transit.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.mad.transit.MainActivity;
import org.mad.transit.R;
import org.mad.transit.database.DatabaseHelper;
import org.mad.transit.sync.InitializeDatabaseTask;

import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    public static final String INITIALIZE_DB_FLAG = "initialize_db_flag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //TODO: Delete this!!!
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        databaseHelper.onUpgrade(databaseHelper.getWritableDatabase(), 0, 1);

        final SharedPreferences sharedPreferences = this.getSharedPreferences(this.getString(R.string.favourites_preference_file_key), Context.MODE_PRIVATE);
        boolean initializeDBFlag = sharedPreferences.getBoolean(INITIALIZE_DB_FLAG, false);
        if (!initializeDBFlag) {
            //INIT DATABASE AND START MAIN ACTIVITY AFTER FINISHED
            new InitializeDatabaseTask(this, new InitializeDatabaseTask.TaskListener() {
                @Override
                public void onFinished() {
                    sharedPreferences.edit().putBoolean(INITIALIZE_DB_FLAG, true).apply();
                    startMainActivity();
                }
            }).execute();
        }else{
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    startMainActivity();
                }
            }, 500);
        }
    }

    private void startMainActivity(){
        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        finish();
    }
}
