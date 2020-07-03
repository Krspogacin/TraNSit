package org.mad.transit.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;

import com.google.android.material.snackbar.Snackbar;

import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.activities.PlacesActivity;
import org.mad.transit.activities.RoutesActivity;
import org.mad.transit.model.Location;
import org.mad.transit.model.PastDirection;
import org.mad.transit.repository.LocationRepository;
import org.mad.transit.repository.PastDirectionRepository;
import org.mad.transit.search.SearchOptions;

import java.util.Calendar;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static org.mad.transit.util.Constants.DEFAULT_SOLUTION_COUNT_OPTION;
import static org.mad.transit.util.Constants.DEFAULT_TIME_OPTION;
import static org.mad.transit.util.Constants.SEARCH_SOLUTION_COUNT_OPTIONS;
import static org.mad.transit.util.Constants.SEARCH_TIME_OPTIONS;
import static org.mad.transit.util.Constants.getDefaultSearchOptions;

public class DirectionsFragment extends Fragment {

    private EditText startPoint;
    private EditText endPoint;
    private Location startLocation;
    private Location endLocation;
    private static final int START_POINT_CODE = 1;
    private static final int END_POINT_CODE = 2;
    public static final String START_POINT = "START";
    public static final String END_POINT = "END";
    public static final String SEARCH_OPTIONS = "OPTIONS";

    private SearchOptions searchOptions;
    private String selectedStartTimeOption;

    @Inject
    LocationRepository locationRepository;

    @Inject
    PastDirectionRepository pastDirectionRepository;

    public static DirectionsFragment newInstance() {
        return new DirectionsFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {

        ((TransitApplication) this.getActivity().getApplicationContext()).getAppComponent().inject(this);

        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.directions_fragment, container, false);

        this.startPoint = view.findViewById(R.id.start_point);
        this.startPoint.setOnClickListener(v -> {
            Intent intent = new Intent(DirectionsFragment.this.getActivity(), PlacesActivity.class);
            DirectionsFragment.this.startActivityForResult(intent, DirectionsFragment.START_POINT_CODE);
        });
        this.endPoint = view.findViewById(R.id.end_point);
        this.endPoint.setOnClickListener(v -> {
            Intent intent = new Intent(DirectionsFragment.this.getActivity(), PlacesActivity.class);
            DirectionsFragment.this.startActivityForResult(intent, DirectionsFragment.END_POINT_CODE);
        });

        LinearLayout routeSettings = view.findViewById(R.id.route_settings);

        Button routeButton = view.findViewById(R.id.button_route_settings);
        routeButton.setOnClickListener(v -> {
            if (View.GONE == routeSettings.getVisibility()) {
                TransitionManager.beginDelayedTransition(routeSettings, new AutoTransition());
                routeSettings.setVisibility(View.VISIBLE);
                routeButton.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_up_gray_24);
            } else {
                TransitionManager.beginDelayedTransition(routeSettings, new AutoTransition());
                routeSettings.setVisibility(View.GONE);
                routeButton.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_down_gray_24);
            }
        });

        searchOptions = getDefaultSearchOptions();
        selectedStartTimeOption = DEFAULT_TIME_OPTION;

        Spinner solutionCountSpinner = view.findViewById(R.id.routes_count_dropdown);
        solutionCountSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, SEARCH_SOLUTION_COUNT_OPTIONS));
        solutionCountSpinner.setSelection(SEARCH_SOLUTION_COUNT_OPTIONS.indexOf(DEFAULT_SOLUTION_COUNT_OPTION));
        solutionCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchOptions.setSolutionCount(SEARCH_SOLUTION_COUNT_OPTIONS.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Switch transferSwitch = view.findViewById(R.id.transfers_enabled_switch);
        transferSwitch.setChecked(searchOptions.isTransfersEnabled());
        transferSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> searchOptions.setTransfersEnabled(isChecked));

        TimePicker timePicker = view.findViewById(R.id.start_time_picker);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(searchOptions.getHours());
        timePicker.setCurrentMinute(searchOptions.getMinutes());
        timePicker.setOnTimeChangedListener((view1, hourOfDay, minute) -> {
            searchOptions.setHours(hourOfDay);
            searchOptions.setMinutes(minute);
        });

        Spinner timeSpinner = view.findViewById(R.id.start_time_dropdown);
        timeSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, SEARCH_TIME_OPTIONS));
        timeSpinner.setSelection(SEARCH_TIME_OPTIONS.indexOf(DEFAULT_TIME_OPTION));
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStartTimeOption = SEARCH_TIME_OPTIONS.get(position);
                boolean defaultTimeOption = selectedStartTimeOption.equals(DEFAULT_TIME_OPTION);
                if (!defaultTimeOption) {
                    // refresh time whenever second option is selected
                    Calendar calendar = Calendar.getInstance();
                    searchOptions.setHours(calendar.get(Calendar.HOUR_OF_DAY));
                    searchOptions.setMinutes(calendar.get(Calendar.MINUTE));
                    timePicker.setCurrentHour(searchOptions.getHours());
                    timePicker.setCurrentMinute(searchOptions.getMinutes());
                }
                timePicker.setVisibility(!defaultTimeOption ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button searchButton = view.findViewById(R.id.button_search);
        searchButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(DirectionsFragment.this.startPoint.getText())) {
                String errorText = DirectionsFragment.this.getString(R.string.field_is_required, DirectionsFragment.this.getString(R.string.start_point));
                DirectionsFragment.this.startPoint.setError(errorText);
                View view1 = DirectionsFragment.this.getActivity().findViewById(android.R.id.content);
                final Snackbar snackbar = Snackbar.make(view1, errorText, Snackbar.LENGTH_SHORT);
                snackbar.setAction(R.string.dismiss_snack_bar, v1 -> snackbar.dismiss());
                snackbar.show();
            } else if (TextUtils.isEmpty(DirectionsFragment.this.endPoint.getText())) {
                String errorText = DirectionsFragment.this.getString(R.string.field_is_required, DirectionsFragment.this.getString(R.string.end_point));
                DirectionsFragment.this.endPoint.setError(errorText);
                View view1 = DirectionsFragment.this.getActivity().findViewById(android.R.id.content);
                final Snackbar snackbar = Snackbar.make(view1, errorText, Snackbar.LENGTH_SHORT);
                snackbar.setAction(R.string.dismiss_snack_bar, v12 -> snackbar.dismiss());
                snackbar.show();
            } else {
                saveDirection();

                Intent intent = new Intent(DirectionsFragment.this.getContext(), RoutesActivity.class);
                intent.putExtra(DirectionsFragment.START_POINT, startLocation);
                intent.putExtra(DirectionsFragment.END_POINT, endLocation);

                boolean defaultStartTime = selectedStartTimeOption.equals(DEFAULT_TIME_OPTION);
                if (defaultStartTime) {
                    Calendar calendar = Calendar.getInstance();
                    searchOptions.setHours(calendar.get(Calendar.HOUR_OF_DAY));
                    searchOptions.setMinutes(calendar.get(Calendar.MINUTE));
                }
                intent.putExtra(DirectionsFragment.SEARCH_OPTIONS, searchOptions);
                DirectionsFragment.this.startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Location location = (Location) data.getSerializableExtra(PlacesActivity.LOCATION_KEY);
            if (location != null) {
                if (requestCode == START_POINT_CODE) {
                    this.startPoint.setError(null);
                    this.startPoint.setText(location.getName());
                    this.startLocation = location;
                } else if (requestCode == END_POINT_CODE) {
                    this.endPoint.setError(null);
                    this.endPoint.setText(location.getName());
                    this.endLocation = location;
                }
            }
        }
    }

    private void saveDirection() {
        Long startLocationId = DirectionsFragment.this.locationRepository.save(DirectionsFragment.this.startLocation);
        Long endLocationId = DirectionsFragment.this.locationRepository.save(DirectionsFragment.this.endLocation);

        PastDirection pastDirection = DirectionsFragment.this.pastDirectionRepository.findByStartLocationAndEndLocation(
                startLocationId,
                endLocationId);

        if (pastDirection != null) {
            DirectionsFragment.this.pastDirectionRepository.update(pastDirection);
        } else {
            DirectionsFragment.this.pastDirectionRepository.save(startLocationId, endLocationId);
        }
    }
}
