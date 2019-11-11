package com.example.workouttimer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class EditWorkout extends AppCompatActivity {

    private Workout workout;
    private LayoutInflater mInflater;
    private ListView lvExercises;
    private String currFile;
    private String fromActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnSave = findViewById(R.id.btnSave);

        // get intent
        if (getIntent().getParcelableExtra("WORKOUT") != null){
            workout = getIntent().getParcelableExtra("WORKOUT");
            EditText etName = findViewById(R.id.etName);
            etName.setText(workout.getName());
        } else {
            workout = new Workout("", "", 0, 0, new ArrayList<Exercise>());
        }
        fromActivity = getIntent().getStringExtra("ACTIVITY");
        currFile = workout.getFileName();

        // inflate list view
        lvExercises = findViewById(R.id.lvExercises);
        mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup footer = (ViewGroup)mInflater.inflate(R.layout.row_footer_add_exercise, lvExercises, false);
        lvExercises.addFooterView(footer, null, false);
        TextView tvAdd = footer.findViewById(R.id.tvAdd);
        lvExercises.setAdapter(new EditWorkout.RowIconAdapter(EditWorkout.this, R.layout.row_edit_workout, R.id.tvExerciseTitle, workout.getExerciseList()));

        // initialise onClickListeners
        btnBack.setOnClickListener(backClickListener);
        btnSave.setOnClickListener(saveClickListener);
        tvAdd.setOnClickListener(addClickListener);
        lvExercises.setOnItemClickListener(exerciseClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_workout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            AddExercise();
        }
        else if (id == R.id.action_delete) {
            DeleteMessage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(requestCode == 0 && resultCode == RESULT_OK && intent != null) {
            if(intent.getBooleanExtra("EXOK", true)){
                Exercise newExercise = intent.getParcelableExtra("EXERCISE");
                int index = intent.getIntExtra("INDEX", -1);
                workout.Add(newExercise, index);
                lvExercises.setAdapter(new EditWorkout.RowIconAdapter(EditWorkout.this, R.layout.row_edit_workout, R.id.tvExerciseTitle, workout.getExerciseList()));
            } else {
                DeleteExercise(intent.getIntExtra("INDEX", -1));
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                lvExercises.setAdapter(new EditWorkout.RowIconAdapter(EditWorkout.this, R.layout.row_edit_workout, R.id.tvExerciseTitle, workout.getExerciseList()));
            }
        } else {
            Log.d("EDITW", "save canceled");
        }
    }

    // go back to Main Activity
    View.OnClickListener backClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    // start exercise
    View.OnClickListener saveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SaveToFile();
            if(fromActivity.matches("MainActivity")){
                Intent intent = new Intent(EditWorkout.this, MainActivity.class);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else if (fromActivity.matches("ViewWorkout")){
                Intent intent = new Intent(EditWorkout.this, ViewWorkout.class);
                intent.putExtra("WORKOUT", workout.getName());
                intent.putExtra("FILEOK", true);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                Log.d("TEST", "Error");
            }
        }
    };

    // go back to Main Activity
    View.OnClickListener addClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddExercise();
        }
    };

    AdapterView.OnItemClickListener exerciseClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(EditWorkout.this, EditExercise.class);
            intent.putExtra("WORKOUT", workout.getExerciseListItem(position));
            intent.putExtra("INDEX", position);
            startActivityForResult(intent, 0);
        }
    };

    // View holder for workout list
    public class exerciseViewHolder {
        TextView tvExerciseTitle;
        TextView tvExerciseSub;

        public exerciseViewHolder(View view){
            tvExerciseTitle = view.findViewById(R.id.tvExerciseTitle);
            tvExerciseSub = view.findViewById(R.id.tvExerciseSub);
        }
    }

    class RowIconAdapter extends ArrayAdapter<Exercise> {
        private ArrayList<Exercise> workoutList;
        public RowIconAdapter(Context c, int rowResourceId, int textViewResourceId, ArrayList<Exercise> items) {
            super(c, rowResourceId, textViewResourceId, items);
            workoutList  = items;
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            View row = convertView;
            EditWorkout.exerciseViewHolder holder = null;

            if(row == null){
                row = mInflater.inflate(R.layout.row_edit_workout, parent, false);
                holder = new EditWorkout.exerciseViewHolder(row);
                row.setTag(holder);
            } else {
                holder = (EditWorkout.exerciseViewHolder)row.getTag();
            }

            String currWorkout = workoutList.get(pos).getName();
            if (currWorkout != null) {
                String infoWorkout = getData(workoutList.get(pos));
                holder.tvExerciseTitle.setText(currWorkout);
                holder.tvExerciseSub.setText(infoWorkout);
            }
            return row;
        }
    }

    private String getData(Exercise exercise) {
        String text = "";
        if(exercise.getTimer() > 0) {
            text += "Time: " + exercise.getTimer() + "min ";
        }
        if(exercise.getReps() > 1) {
            text += "Reps: " + exercise.getReps() + " ";
        }
        if(exercise.getSets() > 1) {
            text += "Sets: " + exercise.getSets() + " ";
        }
        if(exercise.getWeight() > 0) {
            text += "Weight: " + exercise.getWeight() + "kg ";
        }
        if(exercise.getDistance() > 0) {
            text += "Distance: " + exercise.getDistance() + "m ";
        }

        return text;
    }

    // calls activity to add exercise
    private void AddExercise(){
        Intent intent = new Intent(this, EditExercise.class);
        startActivityForResult(intent, 0);
    }

    // prompt user to delete file
    private void DeleteMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditWorkout.this);
        builder.setMessage("Do you want to delete? This action cannot be undone.");
        builder.setTitle("Alert");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(DeleteWorkout(currFile)){
                    Toast.makeText(EditWorkout.this, "File deleted", Toast.LENGTH_SHORT).show();
                    Intent intent;
                    if(fromActivity.matches("MainActivity")){
                        intent = new Intent(EditWorkout.this, MainActivity.class);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                    else if (fromActivity.matches("ViewWorkout")) {
                        intent = new Intent(EditWorkout.this, ViewWorkout.class);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                }
                else {
                    Toast.makeText(EditWorkout.this, "No file found", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(EditWorkout.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // deletes the selected file
    private boolean DeleteWorkout(String filename){
        File deleteFile = new File(getFilesDir(), filename);
        return deleteFile.delete();
    }

    private void DeleteExercise(int index){
        workout.Remove(index, EditWorkout.this);
    }

    // saves changes to file
    private void SaveToFile(){
        EditText etName = findViewById(R.id.etName);
        if(etName.getText().toString().matches("")){
            Toast.makeText(this, "Please enter a name for your exercise.", Toast.LENGTH_LONG).show();
        } else {
            // get names for file and workout
            String filename = "w" + etName.getText().toString().replaceAll("\\s+", "") + ".txt";
            workout = new Workout(filename, etName.getText().toString(), 60, 1000, workout.getExerciseList());

            // write workout to file
            FileOutputStream oStream = null;
            ArrayList<Exercise> list = workout.getExerciseList();
            try {
                oStream = openFileOutput(filename, MODE_PRIVATE);
                String formatText = workout.getName() + "\n";
                oStream.write(formatText.getBytes());
                for (Exercise exercise : list) {
                    formatText = exercise.getName() + ","
                            + exercise.getBreakTime() + "," + exercise.getTimer() + ","
                            + exercise.getWeight() + "," + exercise.getReps() + ","
                            + exercise.getSets() + "," + exercise.getDistance() + "\n";
                    oStream.write(formatText.getBytes());
                }

                // if file renamed, delete original file
                if (!workout.getFileName().equals(currFile)){
                    DeleteWorkout(currFile);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (oStream != null) {
                    try {
                        oStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
