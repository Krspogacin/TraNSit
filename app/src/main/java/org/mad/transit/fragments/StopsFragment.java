package org.mad.transit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.mad.transit.R;
import org.mad.transit.adapters.StopsAdapter;
import org.mad.transit.model.NearbyStop;
import org.mad.transit.model.StopsFragmentViewModel;

import java.util.ArrayList;

public class StopsFragment extends Fragment implements LifecycleOwner, StopsAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private StopsAdapter stopsAdapter;
    private StopsFragmentViewModel stopsFragmentViewModel;
    private StopsMapFragment stopsMapFragment;

    public static StopsFragment newInstance() {
        return new StopsFragment();
    }

    private final Observer<ArrayList<NearbyStop>> nearbyStopsListUpdateObserver = new Observer<ArrayList<NearbyStop>>() {
        @Override
        public void onChanged(ArrayList<NearbyStop> nearbyStops) {
            StopsFragment.this.stopsAdapter = new StopsAdapter(StopsFragment.this.getActivity(), nearbyStops, StopsFragment.this);
            StopsFragment.this.recyclerView.setLayoutManager(new LinearLayoutManager(StopsFragment.this.getActivity()));
            StopsFragment.this.recyclerView.setAdapter(StopsFragment.this.stopsAdapter);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stops_fragmet, container, false);
        this.recyclerView = view.findViewById(R.id.stops_bottom_sheet_list);
        this.stopsFragmentViewModel = new ViewModelProvider(this).get(StopsFragmentViewModel.class);
        this.stopsFragmentViewModel.getNearbyStopsLiveData().observe(this.getViewLifecycleOwner(), this.nearbyStopsListUpdateObserver);

        this.stopsMapFragment = StopsMapFragment.newInstance(this.stopsFragmentViewModel);
        FragmentTransaction transaction = this.getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.stops_map_container, this.stopsMapFragment).commit();
        return view;
    }

    @Override
    public void onItemClick(int position) {
        NearbyStop nearbyStop = this.stopsFragmentViewModel.getNearbyStopsLiveData().getValue().get(position);
        this.stopsMapFragment.zoomOnLocation(nearbyStop.getLatitude(), nearbyStop.getLongitude());
        this.stopsMapFragment.updateFloatingLocationButton(false);
        this.stopsMapFragment.getNearbyStopsMarkers().get(position).showInfoWindow();
    }
}