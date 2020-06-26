package org.mad.transit.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.dto.RouteDto;
import org.mad.transit.model.Location;
import org.mad.transit.util.LocationsUtil;

import lombok.Setter;

@Setter
public class RoutesMapFragment extends MapFragment {

    private Location startLocation;
    private Location endLocation;
    private RouteDto selectedRoute;
    private View floatingLocationButtonContainer;

    public static RoutesMapFragment newInstance() {
        return new RoutesMapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        ((TransitApplication) this.getActivity().getApplicationContext()).getAppComponent().inject(this);

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
            this.putViewsAboveBottomSheet(bottomSheet, bottomSheetHeader.getHeight(), this.floatingLocationButtonContainer);
        }

        this.googleMap.setOnMapLoadedCallback(() -> {
            setOnInfoWindowClickListener();
            zoomOnDefaultBounds();
        });

        this.googleMap.setOnCameraIdleListener(() -> {
            addLocationMarker(startLocation);
            addLocationMarker(endLocation);
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
                RoutesMapFragment.this.startNavigation();
            }
        });
    }

    private void startNavigation() {
//        Intent intent = new Intent(this.getActivity(), NavigationActivity.class);
//        intent.putExtra(NavigationActivity.ROUTE, this.selectedRoute);
//        this.startActivity(intent);
        Toast.makeText(getActivity(), "Not yet implemented!", Toast.LENGTH_SHORT).show();
    }

    public void zoomOnRoute(LatLngBounds routeBounds) {
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(routeBounds, 100));
    }
}
