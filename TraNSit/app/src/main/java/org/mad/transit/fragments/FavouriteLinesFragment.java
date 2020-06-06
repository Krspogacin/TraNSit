package org.mad.transit.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.activities.SingleLineActivity;
import org.mad.transit.adapters.FavouriteLinesAdapter;
import org.mad.transit.model.Line;
import org.mad.transit.view.model.LineViewModel;

import java.util.Set;

import javax.inject.Inject;

public class FavouriteLinesFragment extends ListFragment {

    private static Set<String> lineNumbers;
    private FavouriteLinesAdapter adapter;

    @Inject
    LineViewModel lineViewModel;

    public static FavouriteLinesFragment newInstance(Set<String> lineNumbers) {
        FavouriteLinesFragment.lineNumbers = lineNumbers;
        return new FavouriteLinesFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {

        ((TransitApplication) this.getActivity().getApplicationContext()).getAppComponent().inject(this);

        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.adapter = new FavouriteLinesAdapter(this.getActivity(), this.lineViewModel.getLinesByNumbers(lineNumbers));
        this.setListAdapter(this.adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this.getContext(), SingleLineActivity.class);
        Line line = this.lineViewModel.getLinesByNumbers(lineNumbers).get(position);
        intent.putExtra(SingleLineActivity.LINE_KEY, line);
        this.getContext().startActivity(intent);
    }

    public FavouriteLinesAdapter getAdapter() {
        return this.adapter;
    }
}