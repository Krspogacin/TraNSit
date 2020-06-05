package org.mad.transit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mad.transit.R;
import org.mad.transit.activities.TimetableActivity;
import org.mad.transit.adapters.TimetableListAdapter;
import org.mad.transit.model.DepartureTime;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

public class TimetableFragment extends ListFragment {
    private String day;

    public static TimetableFragment newInstance(String day) {
        return new TimetableFragment(day);
    }

    private TimetableFragment(String day){
        this.day = day;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TimetableListAdapter adapter = new TimetableListAdapter(getActivity(), day);
        setListAdapter(adapter);
    }
}
