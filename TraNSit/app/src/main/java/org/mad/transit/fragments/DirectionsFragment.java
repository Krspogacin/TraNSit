package org.mad.transit.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.mad.transit.R;
import org.mad.transit.activities.PlacesActivity;
import org.mad.transit.activities.RoutesActivity;
import org.mad.transit.model.DirectionsViewModel;

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

        this.startPoint = view.findViewById(R.id.start_point);
        this.startPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DirectionsFragment.this.getActivity(), PlacesActivity.class);
                DirectionsFragment.this.startActivityForResult(intent, DirectionsFragment.START_POINT_CODE);
            }
        });
        this.endPoint = view.findViewById(R.id.end_point);
        this.endPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DirectionsFragment.this.getActivity(), PlacesActivity.class);
                DirectionsFragment.this.startActivityForResult(intent, DirectionsFragment.END_POINT_CODE);
            }
        });

        Button searchButton = view.findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(DirectionsFragment.this.startPoint.getText())) {
                    String errorText = DirectionsFragment.this.getString(R.string.start_point) + " je obavezno!";
                    DirectionsFragment.this.startPoint.setError(errorText);
                    Toast.makeText(DirectionsFragment.this.getActivity(), errorText, Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(DirectionsFragment.this.endPoint.getText())) {
                    String errorText = DirectionsFragment.this.getString(R.string.end_point) + " je obavezno!";
                    DirectionsFragment.this.endPoint.setError(errorText);
                    Toast.makeText(DirectionsFragment.this.getActivity(), errorText, Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(DirectionsFragment.this.getContext(), RoutesActivity.class);
                    intent.putExtra(DirectionsFragment.START_POINT, DirectionsFragment.this.startPoint.getText().toString());
                    intent.putExtra(DirectionsFragment.END_POINT, DirectionsFragment.this.endPoint.getText().toString());
                    DirectionsFragment.this.startActivity(intent);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            String location = (String) data.getSerializableExtra(PlacesActivity.LOCATION_KEY);
            if (location != null) {
                if (requestCode == START_POINT_CODE) {
                    this.startPoint.setError(null);
                    this.startPoint.setText(location);
                } else if (requestCode == END_POINT_CODE) {
                    this.endPoint.setError(null);
                    this.endPoint.setText(location);
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mViewModel = new ViewModelProvider(this).get(DirectionsViewModel.class);
        // TODO: Use the ViewModel
    }

}
