package com.example.workouttimer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class EditExercise extends AppCompatActivity {

    private Exercise exercise;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_exercise);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnSave = findViewById(R.id.btnSave);
        CheckBox cbTimer = findViewById(R.id.cbTimer);
        CheckBox cbWeight = findViewById(R.id.cbWeight);
        CheckBox cbReps = findViewById(R.id.cbReps);
        CheckBox cbSets = findViewById(R.id.cbSets);
        CheckBox cbDistance = findViewById(R.id.cbDistance);

        btnBack.setOnClickListener(backClickListener);
        btnSave.setOnClickListener(saveClickListener);
        cbTimer.setOnClickListener(timerClickListener);
        cbWeight.setOnClickListener(weightClickListener);
        cbReps.setOnClickListener(repsClickListener);
        cbSets.setOnClickListener(setsClickListener);
        cbDistance.setOnClickListener(distanceClickListener);

        if(getIntent().getExtras() != null){
            exercise = getIntent().getParcelableExtra("WORKOUT");
            PopulateViews();
        }
        index = getIntent().getIntExtra("INDEX", -1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_exercise, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            DeleteMessage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // go back to Main Activity
    View.OnClickListener backClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    // save exercise
    View.OnClickListener saveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SaveExercise();
        }
    };

    // prompts and un-prompts user to enter exercise information
    View.OnClickListener timerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckBox cbTimer = findViewById(R.id.cbTimer);
            EditText etTimer = findViewById(R.id.etTimer);
            if(cbTimer.isChecked()){
                etTimer.setVisibility(View.VISIBLE);
            } else {
                etTimer.setVisibility(View.GONE);
            }
        }
    };

    View.OnClickListener weightClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckBox cbWeight = findViewById(R.id.cbWeight);
            EditText etWeight = findViewById(R.id.etWeight);
            if(cbWeight.isChecked()){
                etWeight.setVisibility(View.VISIBLE);
            } else {
                etWeight.setVisibility(View.GONE);
            }
        }
    };

    View.OnClickListener repsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckBox cbReps = findViewById(R.id.cbReps);
            EditText etReps = findViewById(R.id.etReps);
            if(cbReps.isChecked()){
                etReps.setVisibility(View.VISIBLE);
            } else {
                etReps.setVisibility(View.GONE);
            }
        }
    };

    View.OnClickListener setsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckBox cbSets = findViewById(R.id.cbSets);
            EditText etSets = findViewById(R.id.etSets);
            if(cbSets.isChecked()){
                etSets.setVisibility(View.VISIBLE);
            } else {
                etSets.setVisibility(View.GONE);
            }
        }
    };

    View.OnClickListener distanceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckBox cbDistance = findViewById(R.id.cbDistance);
            EditText etDistance = findViewById(R.id.etDistance);
            if(cbDistance.isChecked()){
                etDistance.setVisibility(View.VISIBLE);
            } else {
                etDistance.setVisibility(View.GONE);
            }
        }
    };

    // prompt to delete current exercise
    private void DeleteMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditExercise.this);
        builder.setMessage("Do you want to delete? This action cannot be undone.");
        builder.setTitle("Alert");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeleteExercise();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(EditExercise.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // sends intent to delete exercise
    private void DeleteExercise(){
        Intent intent = new Intent(EditExercise.this, EditWorkout.class);
        intent.putExtra("INDEX", index);
        intent.putExtra("EXOK", false);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    // initialised the views when first created
    private void PopulateViews(){
        EditText etName = findViewById(R.id.etName);
        EditText etBreak = findViewById(R.id.etBreak);
        etName.setText(exercise.getName());
        etBreak.setText(Integer.toString(exercise.getBreakTime()));

        CheckBox cbTimer = findViewById(R.id.cbTimer);
        EditText etTimer = findViewById(R.id.etTimer);
        if(exercise.getTimer() > 0){
            cbTimer.setChecked(true);
            etTimer.setVisibility(View.VISIBLE);
            etTimer.setText(Integer.toString(exercise.getTimer()));
        }

        CheckBox cbWeight = findViewById(R.id.cbWeight);
        EditText etWeight = findViewById(R.id.etWeight);
        if(exercise.getWeight() > 0){
            cbWeight.setChecked(true);
            etWeight.setVisibility(View.VISIBLE);
            etWeight.setText(Integer.toString(exercise.getWeight()));
        }

        CheckBox cbReps = findViewById(R.id.cbReps);
        EditText etReps = findViewById(R.id.etReps);
        if(exercise.getReps() > 1){
            cbReps.setChecked(true);
            etReps.setVisibility(View.VISIBLE);
            etReps.setText(Integer.toString(exercise.getReps()));
        }

        CheckBox cbSets = findViewById(R.id.cbSets);
        EditText etSets = findViewById(R.id.etSets);
        if(exercise.getSets() > 1){
            cbSets.setChecked(true);
            etSets.setVisibility(View.VISIBLE);
            etSets.setText(Integer.toString(exercise.getSets()));
        }

        CheckBox cbDistance = findViewById(R.id.cbDistance);
        EditText etDistance = findViewById(R.id.etDistance);
        if(exercise.getDistance() > 0){
            cbDistance.setChecked(true);
            etDistance.setVisibility(View.VISIBLE);
            etDistance.setText(Integer.toString(exercise.getDistance()));
        }
    }

    // sends exercise object to previous activity
    private void SaveExercise(){
        String exName;
        int exBreak, exTimer, exWeight, exReps, exSets, exDistance;

        EditText etName = findViewById(R.id.etName);
        EditText etBreak = findViewById(R.id.etBreak);
        EditText etTimer = findViewById(R.id.etTimer);
        EditText etWeight = findViewById(R.id.etWeight);
        EditText etReps = findViewById(R.id.etReps);
        EditText etSets = findViewById(R.id.etSets);
        EditText etDistance = findViewById(R.id.etDistance);

        if(etName.getText().toString().matches("") || etBreak.getText().toString().matches("")){
            Toast.makeText(this, "Please enter a name and break time.", Toast.LENGTH_LONG).show();
        } else {
            exName = etName.getText().toString();
            exBreak = Integer.parseInt(etBreak.getText().toString());

            exTimer = AssignValueFromEditText(etTimer.getText().toString(), 0);
            exWeight = AssignValueFromEditText(etWeight.getText().toString(), 0);
            exReps = AssignValueFromEditText(etReps.getText().toString(), 1);
            exSets = AssignValueFromEditText(etSets.getText().toString(), 1);
            exDistance = AssignValueFromEditText(etDistance.getText().toString(), 0);

            exercise = new Exercise(exName, exBreak, exTimer, exWeight, exReps, exSets, exDistance);
            Intent intent = new Intent(EditExercise.this, EditWorkout.class);
            intent.putExtra("EXERCISE", exercise);
            intent.putExtra("INDEX", index);
            intent.putExtra("EXOK", true);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    // ensures that data entered is not invalid
    private int AssignValueFromEditText(String data, int defaultValue){
        if(data.matches("")){
            return defaultValue;
        }
        if (Integer.parseInt(data) < defaultValue) {
           return defaultValue;
        }
        return Integer.parseInt(data);
    }


}
