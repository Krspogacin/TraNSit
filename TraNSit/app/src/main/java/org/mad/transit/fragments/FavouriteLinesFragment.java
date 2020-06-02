package org.mad.transit.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.mad.transit.R;
import org.mad.transit.activities.SingleLineActivity;
import org.mad.transit.adapters.FavouriteLinesAdapter;
import org.mad.transit.model.Line;
import org.mad.transit.view.model.LinesFragmentViewModel;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

public class FavouriteLinesFragment extends ListFragment {

    private static Set<String> lineNumbers;
    private FavouriteLinesAdapter adapter;

    public static FavouriteLinesFragment newInstance(Set<String> lineNumbers) {
        FavouriteLinesFragment.lineNumbers = lineNumbers;
        return new FavouriteLinesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.adapter = new FavouriteLinesAdapter(this.getActivity(), LinesFragmentViewModel.getLinesByNumbers(lineNumbers));
        this.setListAdapter(this.adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this.getContext(), SingleLineActivity.class);
        Line line = LinesFragmentViewModel.getLinesByNumbers(lineNumbers).get(position);
        intent.putExtra(SingleLineActivity.LINE_KEY, line);
        this.getContext().startActivity(intent);
    }

    public FavouriteLinesAdapter getAdapter() {
        return this.adapter;
    }
}