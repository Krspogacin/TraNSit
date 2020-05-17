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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.mad.transit.R;
import org.mad.transit.adapters.StopsAdapter;
import org.mad.transit.model.NearbyStop;
import org.mad.transit.model.StopsViewModel;

import java.util.List;

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
            StopsFragment.this.stopsAdapter = new StopsAdapter(StopsFragment.this.getActivity(), nearbyStops, StopsFragment.this);
            StopsFragment.this.recyclerView.setLayoutManager(new LinearLayoutManager(StopsFragment.this.getActivity()));
            StopsFragment.this.recyclerView.addItemDecoration(new DividerItemDecoration(StopsFragment.this.getContext(), DividerItemDecoration.VERTICAL));
            StopsFragment.this.recyclerView.setAdapter(StopsFragment.this.stopsAdapter);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stops_fragment, container, false);
        this.recyclerView = view.findViewById(R.id.stops_bottom_sheet_list);
        this.stopsViewModel = new ViewModelProvider(this).get(StopsViewModel.class);
        this.stopsViewModel.getNearbyStopsLiveData().observe(this.getViewLifecycleOwner(), this.nearbyStopsListUpdateObserver);

        this.mapFragment = StopsMapFragment.newInstance(this.stopsViewModel);
        FragmentTransaction transaction = this.getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.stops_map_container, this.mapFragment).commit();
        return view;
    }

    @Override
    public void onItemClick(int position) {
        NearbyStop nearbyStop = this.stopsViewModel.getNearbyStopsLiveData().getValue().get(position);
        this.mapFragment.zoomOnLocation(nearbyStop.getLatitude(), nearbyStop.getLongitude());
        this.mapFragment.updateFloatingLocationButton(false);
        this.mapFragment.getStopMarkers().get(position).showInfoWindow();
    }
}