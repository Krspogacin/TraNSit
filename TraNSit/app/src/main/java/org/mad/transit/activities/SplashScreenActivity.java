package org.mad.transit.activities;

import android.content.Intent;
import android.os.Bundle;

import org.mad.transit.MainActivity;
import org.mad.transit.R;
import org.mad.transit.database.DatabaseHelper;
import org.mad.transit.sync.InitializeDatabaseTask;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        databaseHelper.onUpgrade(databaseHelper.getWritableDatabase(), 0, 1);

        //INIT DATABASE AND START MAIN ACTIVITY AFTER FINISHED
        new InitializeDatabaseTask(this, new InitializeDatabaseTask.TaskListener() {
            @Override
            public void onFinished() {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                finish();
            }
        }).execute();
    }
}
