package org.mad.transit.fragments;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mad.transit.model.Location;

import lombok.Getter;

public class ChooseOnMapFragment extends MapFragment {

    private Marker chosenLocationMarker;
    private static FloatingActionButton confirmButton;

    @Getter
    private Location location;

    public static ChooseOnMapFragment newInstance(FloatingActionButton confirmButton) {
        ChooseOnMapFragment.confirmButton = confirmButton;
        return new ChooseOnMapFragment();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        super.onMapReady(googleMap);

        this.zoomOnLocation(this.defaultLocation.latitude, this.defaultLocation.longitude);

        this.googleMap.setOnMapClickListener(latLng -> {
            ChooseOnMapFragment.this.location = new Location(latLng.latitude, latLng.longitude);
            if (ChooseOnMapFragment.this.chosenLocationMarker != null) {
                ChooseOnMapFragment.this.chosenLocationMarker.remove();
            }
            if (!ChooseOnMapFragment.confirmButton.isEnabled()) {
                ChooseOnMapFragment.confirmButton.setEnabled(true);
            }
            ChooseOnMapFragment.this.chosenLocationMarker = ChooseOnMapFragment.this.googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .position(new LatLng(latLng.latitude, latLng.longitude)));
            ChooseOnMapFragment.this.zoomOnLocation(latLng.latitude, latLng.longitude);
        });
    }
}
