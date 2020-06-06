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

import org.jetbrains.annotations.NotNull;
import org.mad.transit.R;
import org.mad.transit.TransitApplication;
import org.mad.transit.activities.SingleLineActivity;
import org.mad.transit.adapters.LinesAdapter;
import org.mad.transit.model.Line;
import org.mad.transit.view.model.LineViewModel;

import javax.inject.Inject;

public class LinesFragment extends ListFragment {

    @Inject
    LineViewModel lineViewModel;

    public static LinesFragment newInstance() {
        return new LinesFragment();
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
    public void onListItemClick(@NotNull ListView l, @NotNull View v, int position, long id) {
        Intent intent = new Intent(LinesFragment.this.getContext(), SingleLineActivity.class);
        Line line = this.lineViewModel.getLines().get(position);
        intent.putExtra(SingleLineActivity.LINE_KEY, line);
        LinesFragment.this.getContext().startActivity(intent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinesAdapter adapter = new LinesAdapter(this.getActivity(), this.lineViewModel);
        this.setListAdapter(adapter);
    }

}
