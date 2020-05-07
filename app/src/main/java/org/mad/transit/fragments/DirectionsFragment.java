package org.mad.transit.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.mad.transit.R;
import org.mad.transit.activities.ChooseLocationActivity;
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

    public static DirectionsFragment newInstance() {
        return new DirectionsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.directions_fragment, container, false);

        Button searchButton = view.findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Development in progress!", Toast.LENGTH_SHORT).show();

            }
        });
        startPoint = view.findViewById(R.id.start_point_text);
        startPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChooseLocationActivity.class);
                startActivityForResult(intent, START_POINT_CODE);
            }
        });
        endPoint = view.findViewById(R.id.end_point_text);
        endPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChooseLocationActivity.class);
                startActivityForResult(intent, END_POINT_CODE);
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
                    startPoint.setText(suggestion.getText());
                } else if (requestCode == END_POINT_CODE) {
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
