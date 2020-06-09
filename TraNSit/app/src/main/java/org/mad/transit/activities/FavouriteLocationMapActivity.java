package org.mad.transit.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import org.mad.transit.R;
import org.mad.transit.fragments.FavouriteLocationMapFragment;
import org.mad.transit.model.FavouriteLocation;

public class FavouriteLocationMapActivity extends AppCompatActivity {

    public static final String FAVOURITE_LOCATION_KEY = "favourite_location";
    private EditText favouriteLocationTitleInput;
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_favourite_location_map);

        final FavouriteLocation favouriteLocation = (FavouriteLocation) this.getIntent().getSerializableExtra(FAVOURITE_LOCATION_KEY);

        final FavouriteLocationMapFragment favouriteLocationMapFragment = FavouriteLocationMapFragment.newInstance(favouriteLocation);
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.favourite_location_map_container, favouriteLocationMapFragment).commit();

        this.favouriteLocationTitleInput = this.findViewById(R.id.favourite_location_title_input);

        this.inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        final TextView favouriteLocationTextView = this.findViewById(R.id.favourite_location_name);
        favouriteLocationTextView.setText(this.getString(R.string.favourite_location_name, favouriteLocation.getLocation().getName()));

        final Button saveButton = this.findViewById(R.id.favourite_location_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable title = FavouriteLocationMapActivity.this.favouriteLocationTitleInput.getText();
                if (title != null && title.length() > 0) {
                    Intent intent = new Intent();
                    favouriteLocation.setTitle(title.toString());
                    intent.putExtra(FavouriteLocationMapActivity.FAVOURITE_LOCATION_KEY, favouriteLocation);
                    FavouriteLocationMapActivity.this.setResult(Activity.RESULT_OK, intent);
                    FavouriteLocationMapActivity.this.finish();
                } else {
                    View view = FavouriteLocationMapActivity.this.findViewById(android.R.id.content);
                    final Snackbar snackbar = Snackbar.make(view, R.string.empty_favourite_location_title_message, Snackbar.LENGTH_SHORT);
                    snackbar.setAction(R.string.dismiss_snack_bar, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.favouriteLocationTitleInput.requestFocus();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}