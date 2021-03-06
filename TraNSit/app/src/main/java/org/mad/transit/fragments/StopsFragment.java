package org.mad.transit.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.Marker;

import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.adapters.StopsAdapter;
import org.mad.transit.model.NearbyStop;
import org.mad.transit.view.model.StopViewModel;

import java.util.List;

import javax.inject.Inject;

public class StopsFragment extends Fragment implements LifecycleOwner, StopsAdapter.OnItemClickListener {

    @Inject
    StopViewModel stopViewModel;

    private RecyclerView recyclerView;
    private StopsMapFragment mapFragment;
    private FrameLayout loadingOverlay;
    private boolean dataAlreadyLoaded;

    public static StopsFragment newInstance() {
        return new StopsFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {

        ((TransitApplication) this.getActivity().getApplicationContext()).getAppComponent().inject(this);

        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stops_fragment, container, false);

        this.loadingOverlay = view.findViewById(R.id.loading_overlay);

        this.recyclerView = view.findViewById(R.id.stops_bottom_sheet_list);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(StopsFragment.this.getActivity()));
        this.recyclerView.addItemDecoration(new DividerItemDecoration(StopsFragment.this.getContext(), DividerItemDecoration.VERTICAL));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (this.dataAlreadyLoaded) {
            this.loadingOverlay.setVisibility(View.GONE);
        }

        this.dataAlreadyLoaded = true;

        if (this.mapFragment == null) {
            this.getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            this.mapFragment = StopsMapFragment.newInstance(this.loadingOverlay);
            FragmentTransaction transaction = this.getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.stops_map_container, this.mapFragment).commit();
        }

        this.stopViewModel.getNearbyStopsLiveData().observe(this.getViewLifecycleOwner(), this.nearbyStopsListUpdateObserver);
        this.mapFragment.putViewsAboveBottomSheet();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onItemClick(int position) {
        NearbyStop nearbyStop = this.stopViewModel.getNearbyStopsLiveData().getValue().get(position);
        this.mapFragment.zoomOnLocation(nearbyStop.getLocation().getLatitude(), nearbyStop.getLocation().getLongitude());
        this.mapFragment.updateFloatingLocationButton(false);
        if (!this.mapFragment.getStopMarkers().isEmpty()) {
            for (Marker marker : this.mapFragment.getStopMarkers()) {
                NearbyStop markerNearbyStop = (NearbyStop) marker.getTag();
                if (markerNearbyStop.equals(nearbyStop)) {
                    marker.showInfoWindow();
                    return;
                }
            }
        }
    }

    private final Observer<List<NearbyStop>> nearbyStopsListUpdateObserver = nearbyStops -> {
        StopsAdapter stopsAdapter = new StopsAdapter(StopsFragment.this.getActivity(), nearbyStops, StopsFragment.this);
        StopsFragment.this.recyclerView.setAdapter(stopsAdapter);

        if (StopsFragment.this.mapFragment != null) {
            StopsFragment.this.mapFragment.updateStopMarkers();
            StopsFragment.this.mapFragment.setViewsPadding();
        }
    };
}