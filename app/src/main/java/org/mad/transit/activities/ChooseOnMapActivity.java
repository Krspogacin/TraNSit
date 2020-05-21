package org.mad.transit.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mad.transit.R;
import org.mad.transit.fragments.ChooseOnMapFragment;

public class ChooseOnMapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_choose_on_map);

        FloatingActionButton confirmButton = this.findViewById(R.id.floating_confirm_button);
        confirmButton.setEnabled(false);

        final ChooseOnMapFragment chooseOnMapFragment = ChooseOnMapFragment.newInstance(confirmButton);
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.choose_on_map_container, chooseOnMapFragment).commit();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(PlacesActivity.LOCATION_KEY, chooseOnMapFragment.getAddress());
                ChooseOnMapActivity.this.setResult(Activity.RESULT_OK, intent);
                ChooseOnMapActivity.this.finish();
            }
        });
    }
}