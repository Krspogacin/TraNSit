package org.mad.transit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mad.transit.R;
import org.mad.transit.adapters.TimetableListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

public class TimetableFragment extends ListFragment {

    public static TimetableFragment newInstance() {
        return new TimetableFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lines_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TimetableListAdapter adapter = new TimetableListAdapter(this.getActivity());
        setListAdapter(adapter);
    }
}
