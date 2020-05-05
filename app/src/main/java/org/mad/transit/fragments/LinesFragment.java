package org.mad.transit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.mad.transit.R;
import org.mad.transit.adapters.LinesAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

public class LinesFragment extends ListFragment {

    public static LinesFragment newInstance() {
        return new LinesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
      return inflater.inflate(R.layout.lines_fragment, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        //TODO: add on list item click action
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinesAdapter adapter = new LinesAdapter(this.getActivity());
        setListAdapter(adapter);
    }

}
