package org.mad.transit.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.activities.NavigationActivity;
import org.mad.transit.dto.RouteDto;
import org.mad.transit.model.Location;
import org.mad.transit.navigation.NavigationService;
import org.mad.transit.util.LocationsUtil;

import java.util.List;

import lombok.Setter;

@Setter
public class RoutesMapFragment extends MapFragment {

    private Location startLocation;
    private Location endLocation;
    private RouteDto selectedRoute;
    private View floatingLocationButtonContainer;
    private SharedPreferences defaultSharedPreferences;

    public static RoutesMapFragment newInstance() {
        return new RoutesMapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        ((TransitApplication) this.getActivity().getApplicationContext()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        this.registerLocationSettingsChangedReceiver();
        this.defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        this.configAboutFloatingLocationButton();

        View bottomSheetHeader = this.getActivity().findViewById(R.id.bottom_sheet_header);
        if (bottomSheetHeader != null) {
            View bottomSheet = this.getActivity().findViewById(R.id.bottom_sheet);
            this.putViewsAboveBottomSheet(bottomSheet, bottomSheetHeader.getHeight(), this.floatingLocationButtonContainer);
        }

        this.googleMap.setOnMapLoadedCallback(() -> {
            this.setOnInfoWindowClickListener();
            this.includeLocationsAndZoomOnBounds(this.startLocation.getLatitude(), this.startLocation.getLongitude(), this.endLocation.getLatitude(), this.endLocation.getLongitude());
        });

        this.googleMap.setOnCameraIdleListener(() -> {
            this.addLocationMarker(this.startLocation);
            this.addLocationMarker(this.endLocation);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LocationsUtil.LOCATION_REQUEST_CHECK_SETTINGS &&
                resultCode == Activity.RESULT_OK &&
                LocationsUtil.locationPermissionsGranted(this.getActivity())) {
            this.startNavigation();
        }
    }

    private void configAboutFloatingLocationButton() {
        this.floatingLocationButtonContainer = this.getActivity().findViewById(R.id.floating_location_button_container);

        FloatingActionButton floatingActionButton = this.getActivity().findViewById(R.id.floating_location_button);

        floatingActionButton.setOnClickListener(v -> {
            if (RoutesMapFragment.this.selectedRoute == null) {
                View view = RoutesMapFragment.this.getActivity().findViewById(android.R.id.content);
                final Snackbar snackbar = Snackbar.make(view, R.string.route_not_chosen_message, Snackbar.LENGTH_SHORT);
                snackbar.setAction(R.string.dismiss_snack_bar, v1 -> snackbar.dismiss());
                snackbar.show();
            } else if (!LocationsUtil.locationSettingsAvailability(RoutesMapFragment.this.locationManager) ||
                    !LocationsUtil.locationPermissionsGranted(RoutesMapFragment.this.getActivity())) {
                RoutesMapFragment.this.runLocationUpdates();
            } else {
                RoutesMapFragment.this.checkIfThereAreActiveService();
            }
        });
    }

    private void startNavigation() {
        Intent intent = new Intent(this.getActivity(), NavigationActivity.class);
        intent.putExtra(NavigationActivity.ROUTE, this.selectedRoute);
        intent.putExtra(NavigationActivity.START_LOCATION, this.startLocation);
        intent.putExtra(NavigationActivity.END_LOCATION, this.endLocation);
        this.startActivity(intent);
    }

    private void checkIfThereAreActiveService() {
        ActivityManager activityManager = (ActivityManager) this.getActivity().getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(50);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            if (runningServiceInfo.service.getClassName().equals(NavigationService.class.getName()) && runningServiceInfo.foreground) {

                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            dialog.dismiss();
                            this.defaultSharedPreferences.edit().putBoolean(this.getString(R.string.service_active_pref_key), false).apply();
                            Intent serviceIntent = new Intent(this.getActivity(), NavigationService.class);
                            this.getActivity().stopService(serviceIntent);
                            this.startNavigation();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
                builder.setTitle(R.string.navigation_already_exists_title)
                        .setMessage(R.string.navigation_already_exists_message)
                        .setIcon(R.drawable.ic_baseline_directions_bus_24)
                        .setPositiveButton(this.getActivity().getString(R.string.positive_answer), dialogClickListener)
                        .setNegativeButton(this.getActivity().getString(R.string.negative_answer), dialogClickListener)
                        .show();

                return;
            }
        }
        this.startNavigation();
    }
}
