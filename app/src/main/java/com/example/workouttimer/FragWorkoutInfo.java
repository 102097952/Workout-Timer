package com.example.workouttimer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragWorkoutInfo extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout_info, container, false);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        String title  = "Now Running: " + getArguments().getString("WORKOUT");
        tvTitle.setText(title);

        Button btnNext = view.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(nextOnClickListener);

        return view;
    }

    View.OnClickListener nextOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((ViewWorkout)getActivity()).startNext();
        }
    };
}
