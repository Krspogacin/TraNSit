package org.mad.transit.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.mad.transit.R;
import org.mad.transit.model.FavouriteLocation;

import static org.mad.transit.activities.FavouriteLocationMapActivity.FAVOURITE_LOCATION_KEY;

public class FavouriteLocationMapFragment extends MapFragment {

    private FavouriteLocation favouriteLocation;

    public static FavouriteLocationMapFragment newInstance(FavouriteLocation favouriteLocation) {
        FavouriteLocationMapFragment favouriteLocationMapFragment = new FavouriteLocationMapFragment();
        Bundle args = new Bundle();
        args.putSerializable(FAVOURITE_LOCATION_KEY, favouriteLocation);
        favouriteLocationMapFragment.setArguments(args);
        return favouriteLocationMapFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.favouriteLocation = (FavouriteLocation) savedInstanceState.getSerializable(FAVOURITE_LOCATION_KEY);
        } else {
            this.favouriteLocation = (FavouriteLocation) this.getArguments().getSerializable(FAVOURITE_LOCATION_KEY);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        super.onMapReady(googleMap);

        //Zoom and put marker on favourite location
        double latitude = this.favouriteLocation.getLocation().getLatitude();
        double longitude = this.favouriteLocation.getLocation().getLongitude();

        Marker marker = this.googleMap.addMarker(new MarkerOptions()
                .title(this.favouriteLocation.getLocation().getName())
                .icon(this.bitmapDescriptorFromVector(R.drawable.ic_favourite_marker))
                .position(new LatLng(latitude, longitude)));

        marker.showInfoWindow();

        this.zoomOnLocation(latitude, longitude);
    }
}
