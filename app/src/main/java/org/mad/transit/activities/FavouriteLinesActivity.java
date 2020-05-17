package org.mad.transit.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import org.mad.transit.R;
import org.mad.transit.fragments.FavouriteLinesFragment;
import org.mad.transit.model.Line;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FavouriteLinesActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private FavouriteLinesFragment favouriteLinesFragment;

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

        Set<String> lineNumbers = this.sharedPreferences.getStringSet(SingleLineActivity.FAVOURITE_LINES_KEY, new HashSet<String>());
        this.favouriteLinesFragment = FavouriteLinesFragment.newInstance(lineNumbers);
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.favourite_lines_list_container, this.favouriteLinesFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.favourite_line_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Set<String> lineNumbers = this.sharedPreferences.getStringSet(SingleLineActivity.FAVOURITE_LINES_KEY, new HashSet<String>());
        if (!lineNumbers.isEmpty() && item.getItemId() == R.id.action_remove_all_favourites) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.remove_favourite_lines_dialog_title)
                    .setMessage(R.string.remove_favourite_lines_dialog_message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            FavouriteLinesActivity.this.sharedPreferences.edit().clear().apply();
                            FavouriteLinesActivity.this.favouriteLinesFragment.getAdapter().setLines(new ArrayList<Line>());
                            FavouriteLinesActivity.this.favouriteLinesFragment.getAdapter().notifyDataSetChanged();
                            Toast.makeText(FavouriteLinesActivity.this, R.string.favourite_lines_removed_message, Toast.LENGTH_SHORT).show();
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