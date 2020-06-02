package org.mad.transit.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.mad.transit.R;
import org.mad.transit.activities.NavigationActivity;
import org.mad.transit.model.Route;
import org.mad.transit.util.LocationsUtil;

public class RoutesMapFragment extends MapFragment {

    private View floatingLocationButtonContainer;
    private Route selectedRoute;

    public static RoutesMapFragment newInstance() {
        return new RoutesMapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.registerLocationSettingsChangedReceiver();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        this.configAboutFloatingLocationButton();

        View bottomSheetHeader = this.getActivity().findViewById(R.id.bottom_sheet_header);
        if (bottomSheetHeader != null) {
            View bottomSheet = this.getActivity().findViewById(R.id.bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            this.putViewsAboveBottomSheet(bottomSheet, bottomSheetHeader.getHeight(), this.floatingLocationButtonContainer);
        }

        this.setOnInfoWindowClickListener();

//        if (!locationSettingsAvailability() || !locationPermissionsGranted()) {
        this.zoomOnLocation(this.defaultLocation.latitude, this.defaultLocation.longitude); //TODO check what to zoom on
//        }
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

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RoutesMapFragment.this.selectedRoute == null) {
                    View view = RoutesMapFragment.this.getActivity().findViewById(android.R.id.content);
                    final Snackbar snackbar = Snackbar.make(view, R.string.route_not_chosen_message, Snackbar.LENGTH_SHORT);
                    snackbar.setAction(R.string.dismiss_snack_bar, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                } else if (!LocationsUtil.locationSettingsAvailability(RoutesMapFragment.this.locationManager) ||
                        !LocationsUtil.locationPermissionsGranted(RoutesMapFragment.this.getActivity())) {
                    RoutesMapFragment.this.runLocationUpdates();
                } else {
                    RoutesMapFragment.this.startNavigation();
                }
            }
        });
    }

    private void startNavigation() {
        Intent intent = new Intent(this.getActivity(), NavigationActivity.class);
        intent.putExtra(NavigationActivity.ROUTE, this.selectedRoute);
        this.startActivity(intent);
    }

    public void setSelectedRoute(Route selectedRoute) {
        this.selectedRoute = selectedRoute;
    }
}
