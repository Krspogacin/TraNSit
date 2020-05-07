package org.mad.transit.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import org.mad.transit.R;
import org.mad.transit.adapters.SuggestionsAdapter;
import org.mad.transit.model.Suggestion;
import org.mad.transit.model.SuggestionsUtil;

import androidx.appcompat.app.AppCompatActivity;

public class ChooseLocationActivity extends AppCompatActivity {

    private SuggestionsAdapter adapter;
    public static final String LOCATION = "LOCATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_location);

        SearchView searchView = findViewById(R.id.location_search);
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.requestFocus();

        ListView listView = findViewById(R.id.location_list);

        adapter = new SuggestionsAdapter(this, SuggestionsUtil.getSuggestions());
        listView.setAdapter(adapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Suggestion suggestion = adapter.getItem(position);
                if (suggestion != null) {
                    String chooseOnMap = getString(R.string.choose_on_map);
                    String chooseCurrentLocation = getString(R.string.choose_current_location);
                    if (suggestion.getText().equals(chooseOnMap) || suggestion.getText().equals(chooseCurrentLocation)) {
                        Toast.makeText(ChooseLocationActivity.this, suggestion.getText(), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra(LOCATION, suggestion);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                }
            }
        });
    }
}
