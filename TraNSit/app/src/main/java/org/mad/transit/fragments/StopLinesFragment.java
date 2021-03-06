package org.mad.transit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import org.mad.transit.R;
import org.mad.transit.adapters.StopLinesAdapter;
import org.mad.transit.model.Stop;

public class StopLinesFragment extends ListFragment {

    private final Stop stop;

    public static StopLinesFragment newInstance(Stop stop) {
        return new StopLinesFragment(stop);
    }

    private StopLinesFragment(Stop stop) {
        this.stop = stop;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        StopLinesAdapter adapter = new StopLinesAdapter(this.getActivity(), this.stop);
        this.setListAdapter(adapter);
    }
}