package org.mad.transit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import org.mad.transit.R;
import org.mad.transit.adapters.TimetableListAdapter;

public class TimetableFragment extends ListFragment {

    public static TimetableFragment newInstance() {
        return new TimetableFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TimetableListAdapter adapter = new TimetableListAdapter(this.getActivity());
        this.setListAdapter(adapter);
    }
}
