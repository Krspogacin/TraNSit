package org.mad.transit.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mad.transit.R;
import org.mad.transit.fragments.ChooseOnMapFragment;
import org.mad.transit.model.Location;
import org.mad.transit.util.LocationsUtil;

import java.io.IOException;

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

        confirmButton.setOnClickListener(v -> {
            Location location = chooseOnMapFragment.getLocation();
            int result;
            try {
                String address = LocationsUtil.retrieveAddressFromLatAndLng(this, location.getLatitude(), location.getLongitude());
                location.setName(address);
                result = Activity.RESULT_OK;
            } catch (IOException e) {
                result = Activity.RESULT_CANCELED;
            }

            Intent intent = new Intent();
            intent.putExtra(PlacesActivity.LOCATION_KEY, location);
            ChooseOnMapActivity.this.setResult(result, intent);
            ChooseOnMapActivity.this.finish();
        });
    }
}