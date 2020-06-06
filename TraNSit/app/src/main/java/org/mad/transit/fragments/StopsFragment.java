package org.mad.transit.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        ProgressBar progressBar = view.findViewById(R.id.loading_progress_bar);
        progressBar.getIndeterminateDrawable().setColorFilter(this.getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);

        this.recyclerView = view.findViewById(R.id.stops_bottom_sheet_list);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(StopsFragment.this.getActivity()));
        this.recyclerView.addItemDecoration(new DividerItemDecoration(StopsFragment.this.getContext(), DividerItemDecoration.VERTICAL));

        this.stopViewModel.getNearbyStopsLiveData().observe(this.getViewLifecycleOwner(), this.nearbyStopsListUpdateObserver);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.mapFragment == null) {
            this.mapFragment = StopsMapFragment.newInstance();
            FragmentTransaction transaction = this.getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.stops_map_container, this.mapFragment).commit();
        }
    }

    @Override
    public void onItemClick(int position) {
        this.mapFragment.bottomSheetItemClicked = true;
        NearbyStop nearbyStop = this.stopViewModel.getNearbyStopsLiveData().getValue().get(position);
        this.mapFragment.zoomOnLocation(nearbyStop.getLocation().getLatitude(), nearbyStop.getLocation().getLongitude());
        this.mapFragment.updateFloatingLocationButton(false);
        this.mapFragment.getStopMarkers().get(position).showInfoWindow();
    }

    private final Observer<List<NearbyStop>> nearbyStopsListUpdateObserver = new Observer<List<NearbyStop>>() {
        @Override
        public void onChanged(List<NearbyStop> nearbyStops) {
            if (StopsFragment.this.loadingOverlay.getVisibility() == View.VISIBLE) {
                StopsFragment.this.loadingOverlay.setVisibility(View.GONE);
            }

            StopsAdapter stopsAdapter = new StopsAdapter(StopsFragment.this.getActivity(), nearbyStops, StopsFragment.this);
            StopsFragment.this.recyclerView.setAdapter(stopsAdapter);

            if (StopsFragment.this.mapFragment != null) {
                StopsFragment.this.mapFragment.updateStopMarkers();
                StopsFragment.this.mapFragment.setViewsPadding();
            }
        }
    };
}