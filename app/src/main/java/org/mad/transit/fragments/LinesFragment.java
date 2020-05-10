package org.mad.transit.fragments;

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
import org.mad.transit.activities.SingleLineActivity;
import org.mad.transit.adapters.LinesAdapter;
import org.mad.transit.model.Line;
import org.mad.transit.model.LinesFragmentViewModel;

public class LinesFragment extends ListFragment {

    public static LinesFragment newInstance() {
        return new LinesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(LinesFragment.this.getContext(), SingleLineActivity.class);
        Line line = LinesFragmentViewModel.getLines().get(position);
        intent.putExtra(SingleLineActivity.LINE_KEY, line);
        LinesFragment.this.getContext().startActivity(intent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinesAdapter adapter = new LinesAdapter(this.getActivity());
        this.setListAdapter(adapter);
    }

}
