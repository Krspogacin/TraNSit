package org.mad.transit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import org.mad.transit.R;
import org.mad.transit.adapters.FavouriteLinesAdapter;
import org.mad.transit.model.LinesFragmentViewModel;

import java.util.Set;

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

    public FavouriteLinesAdapter getAdapter() {
        return this.adapter;
    }
}