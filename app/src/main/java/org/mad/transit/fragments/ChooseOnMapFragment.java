package org.mad.transit.fragments;

import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mad.transit.util.LocationsUtil;

import java.io.IOException;

import lombok.Getter;

public class ChooseOnMapFragment extends MapFragment {

    private Marker chosenLocationMarker;

    @Getter
    private String address;

    private static FloatingActionButton confirmButton;

    public static ChooseOnMapFragment newInstance(FloatingActionButton confirmButton) {
        ChooseOnMapFragment.confirmButton = confirmButton;
        return new ChooseOnMapFragment();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        super.onMapReady(googleMap);

        this.zoomOnLocation(this.defaultLocation.latitude, this.defaultLocation.longitude);

        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                try {
                    ChooseOnMapFragment.this.address = LocationsUtil.retrieveAddressFromLatAndLng(ChooseOnMapFragment.this.getActivity(), latLng.latitude, latLng.longitude);
                    if (ChooseOnMapFragment.this.chosenLocationMarker != null) {
                        ChooseOnMapFragment.this.chosenLocationMarker.remove();
                    }
                    if (!ChooseOnMapFragment.confirmButton.isEnabled()) {
                        ChooseOnMapFragment.confirmButton.setEnabled(true);
                    }
                    ChooseOnMapFragment.this.chosenLocationMarker = ChooseOnMapFragment.this.googleMap.addMarker(new MarkerOptions()
                            .title(ChooseOnMapFragment.this.address)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            .position(new LatLng(latLng.latitude, latLng.longitude)));
                    ChooseOnMapFragment.this.zoomOnLocation(latLng.latitude, latLng.longitude);
                } catch (IOException e) {
                    Toast.makeText(ChooseOnMapFragment.this.getActivity(), "Doslo je do greške! Pokušajte ponovo", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
