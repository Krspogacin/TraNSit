package org.mad.transit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mad.transit.R;
import org.mad.transit.adapters.StopsAdapter;
import org.mad.transit.model.NearbyStop;
import org.mad.transit.model.StopsViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class StopsFragment extends Fragment implements LifecycleOwner, StopsAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private StopsAdapter stopsAdapter;
    private StopsViewModel stopsViewModel;
    private StopsMapFragment mapFragment;

    public static StopsFragment newInstance() {
        return new StopsFragment();
    }

    private final Observer<List<NearbyStop>> nearbyStopsListUpdateObserver = new Observer<List<NearbyStop>>() {
        @Override
        public void onChanged(List<NearbyStop> nearbyStops) {
            stopsAdapter = new StopsAdapter(getActivity(), nearbyStops, StopsFragment.this);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(stopsAdapter);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stops_fragment, container, false);
        recyclerView = view.findViewById(R.id.stops_bottom_sheet_list);
        stopsViewModel = new ViewModelProvider(this).get(StopsViewModel.class);
        stopsViewModel.getNearbyStopsLiveData().observe(getViewLifecycleOwner(), nearbyStopsListUpdateObserver);

        mapFragment = StopsMapFragment.newInstance(stopsViewModel);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.stops_map_container, mapFragment).commit();
        return view;
    }

    @Override
    public void onItemClick(int position) {
        NearbyStop nearbyStop = stopsViewModel.getNearbyStopsLiveData().getValue().get(position);
        mapFragment.zoomOnLocation(nearbyStop.getLatitude(), nearbyStop.getLongitude());
        mapFragment.updateFloatingLocationButton(false);
        mapFragment.getStopMarkers().get(position).showInfoWindow();
    }
}