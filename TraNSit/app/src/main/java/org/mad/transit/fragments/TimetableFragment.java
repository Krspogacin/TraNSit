package org.mad.transit.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.adapters.TimetableListAdapter;
import org.mad.transit.view.model.TimetableViewModel;

import javax.inject.Inject;

public class TimetableFragment extends ListFragment {
    private final String day;

    @Inject
    TimetableViewModel timetableViewModel;

    public static TimetableFragment newInstance(String day) {
        return new TimetableFragment(day);
    }

    private TimetableFragment(String day) {
        this.day = day;
    }

    @Override
    public void onAttach(@NonNull Context context) {

        ((TransitApplication) this.getActivity().getApplicationContext()).getAppComponent().inject(this);

        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TimetableListAdapter adapter = new TimetableListAdapter(this.getActivity(), this.day, this.timetableViewModel);
        this.setListAdapter(adapter);
    }
}
