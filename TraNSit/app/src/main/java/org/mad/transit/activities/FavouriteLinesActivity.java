package org.mad.transit.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.mad.transit.R;
import org.mad.transit.fragments.FavouriteLinesFragment;
import org.mad.transit.model.Line;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FavouriteLinesActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private FavouriteLinesFragment favouriteLinesFragment;
    private MenuItem deleteAllMenuItem;
    private boolean disableDeleteAllMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_favourite_lines);

        this.sharedPreferences = this.getSharedPreferences(this.getString(R.string.favourites_preference_file_key), Context.MODE_PRIVATE);

        // Show the Up button in the action bar.
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Set<String> lineNumbers = this.sharedPreferences.getStringSet(SingleLineActivity.FAVOURITE_LINES_KEY, new HashSet<String>());

        if (this.deleteAllMenuItem == null) {
            if (lineNumbers.isEmpty()) {
                this.disableDeleteAllMenuItem = true;
            }
        } else {
            if (lineNumbers.isEmpty()) {
                this.deleteAllMenuItem.setEnabled(false);
            } else {
                this.deleteAllMenuItem.setEnabled(true);
            }
        }

        this.favouriteLinesFragment = FavouriteLinesFragment.newInstance(lineNumbers);
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.favourite_lines_list_container, this.favouriteLinesFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.menu_remove_all, menu);

        this.deleteAllMenuItem = menu.findItem(R.id.action_remove_all);

        if (this.disableDeleteAllMenuItem) {
            this.deleteAllMenuItem.setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        Set<String> lineNumbers = this.sharedPreferences.getStringSet(SingleLineActivity.FAVOURITE_LINES_KEY, new HashSet<String>());
        if (!lineNumbers.isEmpty() && item.getItemId() == R.id.action_remove_all) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.remove_favourite_lines_dialog_title)
                    .setMessage(R.string.remove_favourite_lines_dialog_message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            FavouriteLinesActivity.this.sharedPreferences.edit().clear().apply();
                            FavouriteLinesActivity.this.favouriteLinesFragment.getAdapter().setLines(new ArrayList<Line>());
                            FavouriteLinesActivity.this.deleteAllMenuItem.setEnabled(false);
                            View view = FavouriteLinesActivity.this.findViewById(android.R.id.content);
                            final Snackbar snackbar = Snackbar.make(view, R.string.favourite_lines_removed_message, Snackbar.LENGTH_SHORT);
                            snackbar.setAction(R.string.dismiss_snack_bar, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }
}