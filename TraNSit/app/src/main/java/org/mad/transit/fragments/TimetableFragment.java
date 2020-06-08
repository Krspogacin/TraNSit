package org.mad.transit.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.adapters.TimetableListAdapter;
import org.mad.transit.model.DepartureTime;
import org.mad.transit.model.Timetable;
import org.mad.transit.view.model.TimetableViewModel;

import java.util.ArrayList;
import java.util.List;

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

        Timetable timetable = this.timetableViewModel.getTimetableMap().get(this.day);
        List<String> groups = new ArrayList<>();
        List<Spanned> departureTimes = new ArrayList<>();
        if (timetable != null) {
            SpannedString departureTime = new SpannedString("");
            for (DepartureTime departureTimeObject : timetable.getDepartureTimes()) {
                String group = departureTimeObject.getFormattedValue().substring(0, 2);
                if (!groups.contains(group)) {
                    if (departureTime.length() != 0) {
                        departureTimes.add(departureTime);
                        departureTime = new SpannedString("");
                    }

                    groups.add(group);
                }

                CharSequence departureTimeCharSequence = TextUtils.concat(departureTime, " ", Html.fromHtml("<sup>" + departureTimeObject.getFormattedValue().substring(3, 5) + "</sup"));
                departureTime = new SpannedString(departureTimeCharSequence);
            }

            departureTimes.add(departureTime);

            TimetableListAdapter adapter = new TimetableListAdapter(this.getActivity(), groups, departureTimes);
            this.setListAdapter(adapter);
        }
    }
}
