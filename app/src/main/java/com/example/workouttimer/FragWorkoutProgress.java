package com.example.workouttimer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragWorkoutProgress extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout_progress, container, false);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(getArguments().getString("WORKOUT"));

        TextView tvSummary = view.findViewById(R.id.tvSummary);
        int time = getArguments().getInt("TIME") / 60000;
        tvSummary.setText("Time to complete: " +  Integer.toString(time) + "mins");

        return view;
    }
}
