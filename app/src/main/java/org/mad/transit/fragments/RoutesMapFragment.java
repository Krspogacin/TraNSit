package org.mad.transit.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mad.transit.R;
import org.mad.transit.activities.NavigationActivity;
import org.mad.transit.model.Route;
import org.mad.transit.model.RoutesViewModel;

public class RoutesMapFragment extends MapFragment {

    private RoutesViewModel routesViewModel;
    private View floatingLocationButtonContainer;
    private Route selectedRoute;

    public static RoutesMapFragment newInstance(RoutesViewModel routesViewModel) {
        RoutesMapFragment routesMapFragment = new RoutesMapFragment();

        Bundle args = new Bundle();
        args.putSerializable(MapFragment.VIEW_MODEL_ARG, routesViewModel);
        routesMapFragment.setArguments(args);

        return routesMapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            routesViewModel = (RoutesViewModel) savedInstanceState.getSerializable(MapFragment.VIEW_MODEL_ARG);
        } else {
            routesViewModel = (RoutesViewModel) getArguments().getSerializable(MapFragment.VIEW_MODEL_ARG);
        }
        registerLocationSettingsChangedReceiver();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        configAboutFloatingLocationButton();

        View bottomSheetHeader = getActivity().findViewById(R.id.bottom_sheet_header);
        if (bottomSheetHeader != null) {
            View bottomSheet = getActivity().findViewById(R.id.bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            putViewsAboveBottomSheet(bottomSheet, bottomSheetHeader.getHeight(), floatingLocationButtonContainer);
        }

        setOnInfoWindowClickListener();

//        if (!locationSettingsAvailability() || !locationPermissionsGranted()) {
        zoomOnLocation(defaultLocation.latitude, defaultLocation.longitude); //TODO check what to zoom on
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MapFragment.LOCATION_REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK && locationPermissionsGranted()) {
            startNavigation();
        }
    }

    private void configAboutFloatingLocationButton() {
        floatingLocationButtonContainer = getActivity().findViewById(R.id.floating_location_button_container);

        FloatingActionButton floatingActionButton = getActivity().findViewById(R.id.floating_location_button);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedRoute == null) {
                    Toast.makeText(getActivity(), "Ruta nije izabrana!", Toast.LENGTH_SHORT).show();
                } else if (!locationSettingsAvailability() || !locationPermissionsGranted()) {
                    runLocationUpdates();
                } else {
                    startNavigation();
                }
            }
        });
    }

    private void startNavigation() {
        Intent intent = new Intent(getActivity(), NavigationActivity.class);
        intent.putExtra(NavigationActivity.ROUTE, selectedRoute);
        startActivity(intent);
    }

    public void setSelectedRoute(Route selectedRoute) {
        this.selectedRoute = selectedRoute;
    }
}
