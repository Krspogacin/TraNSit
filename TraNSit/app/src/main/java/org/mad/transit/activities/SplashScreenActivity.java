package org.mad.transit.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import org.mad.transit.MainActivity;
import org.mad.transit.R;
import org.mad.transit.sync.InitializeDatabaseTask;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreenActivity extends AppCompatActivity {

    public static final String INITIALIZE_DB_FLAG = "initialize_db_flag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_splash_screen);

        ProgressBar progressBar = this.findViewById(R.id.splash_screen_progress_bar);
        progressBar.getIndeterminateDrawable().setColorFilter(this.getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);

        final SharedPreferences sharedPreferences = this.getSharedPreferences(this.getString(R.string.favourites_preference_file_key), Context.MODE_PRIVATE);
        boolean initializeDBFlag = sharedPreferences.getBoolean(INITIALIZE_DB_FLAG, false);
        if (!initializeDBFlag) {
            //INIT DATABASE AND START MAIN ACTIVITY AFTER FINISHED
            new InitializeDatabaseTask(this, new InitializeDatabaseTask.TaskListener() {
                @Override
                public void onFinished() {
                    sharedPreferences.edit().putBoolean(SplashScreenActivity.INITIALIZE_DB_FLAG, true).apply();
                    SplashScreenActivity.this.startMainActivity();
                }
            }).execute();
        } else {
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
