package org.mad.transit.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.mad.transit.R;
import org.mad.transit.activities.ChooseLocationActivity;
import org.mad.transit.activities.RoutesActivity;
import org.mad.transit.model.DirectionsViewModel;
import org.mad.transit.model.Suggestion;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class DirectionsFragment extends Fragment {

    private DirectionsViewModel mViewModel;
    private EditText startPoint;
    private EditText endPoint;
    private static final int START_POINT_CODE = 1;
    private static final int END_POINT_CODE = 2;
    public static final String START_POINT = "START";
    public static final String END_POINT = "END";

    public static DirectionsFragment newInstance() {
        return new DirectionsFragment();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.directions_fragment, container, false);

        startPoint = view.findViewById(R.id.start_point);
        startPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChooseLocationActivity.class);
                startActivityForResult(intent, START_POINT_CODE);
            }
        });
        endPoint = view.findViewById(R.id.end_point);
        endPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChooseLocationActivity.class);
                startActivityForResult(intent, END_POINT_CODE);
            }
        });

        Button searchButton = view.findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (TextUtils.isEmpty(startPoint.getText())) {
//                    startPoint.setError(getString(R.string.start_point) + " je obavezno!");
//                } else if (TextUtils.isEmpty(endPoint.getText())) {
//                    endPoint.setError(getString(R.string.end_point) + " je obavezno!");
//                } else {
                Intent intent = new Intent(getContext(), RoutesActivity.class);
                intent.putExtra(START_POINT, startPoint.getText().toString());
                intent.putExtra(END_POINT, endPoint.getText().toString());
                startActivity(intent);
//                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Suggestion suggestion = (Suggestion) data.getSerializableExtra(ChooseLocationActivity.LOCATION);
            if (suggestion != null) {
                if (requestCode == START_POINT_CODE) {
                    startPoint.setError(null);
                    startPoint.setText(suggestion.getText());
                } else if (requestCode == END_POINT_CODE) {
                    endPoint.setError(null);
                    endPoint.setText(suggestion.getText());
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DirectionsViewModel.class);
        // TODO: Use the ViewModel
    }

}
