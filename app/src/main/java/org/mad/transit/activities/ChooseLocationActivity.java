package org.mad.transit.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.mad.transit.R;
import org.mad.transit.adapters.SuggestionsAdapter;
import org.mad.transit.model.Suggestion;
import org.mad.transit.model.SuggestionsUtil;

public class ChooseLocationActivity extends AppCompatActivity {

    private SuggestionsAdapter adapter;
    public static final String LOCATION = "LOCATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_choose_location);

        SearchView searchView = this.findViewById(R.id.location_search);
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.requestFocus();

        // Show the Up button in the action bar.
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ListView listView = this.findViewById(R.id.location_list);

        this.adapter = new SuggestionsAdapter(this, SuggestionsUtil.getSuggestions());
        listView.setAdapter(this.adapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ChooseLocationActivity.this.adapter.getFilter().filter(newText);
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Suggestion suggestion = ChooseLocationActivity.this.adapter.getItem(position);
                if (suggestion != null) {
                    String chooseOnMap = ChooseLocationActivity.this.getString(R.string.choose_on_map);
                    String chooseCurrentLocation = ChooseLocationActivity.this.getString(R.string.choose_current_location);
                    if (suggestion.getText().equals(chooseOnMap) || suggestion.getText().equals(chooseCurrentLocation)) {
                        Toast.makeText(ChooseLocationActivity.this, suggestion.getText(), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra(ChooseLocationActivity.LOCATION, suggestion);
                        ChooseLocationActivity.this.setResult(Activity.RESULT_OK, intent);
                        ChooseLocationActivity.this.finish();
                    }
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }
}
