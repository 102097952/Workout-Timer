package com.example.workouttimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> workoutNameList = new ArrayList<>();
    private ArrayList<String> workoutFileList = new ArrayList<>();
    private LayoutInflater mInflater;
    private ListView lvWorkouts;
    private String input;
    private boolean searching;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        lvWorkouts = findViewById(R.id.lvWorkouts);
        setSupportActionBar(toolbar);

        fab.setOnClickListener(fabClickListener);
        lvWorkouts.setOnItemClickListener(listClickListener);

        mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        lvWorkouts.setAdapter(new RowIconAdapter(MainActivity.this, R.layout.row_main_workout, R.id.tvExerciseTitle, workoutNameList));

        LoadFiles();

        input = "";
        searching = false;

        createNotifyChannel();
    }

    private void createNotifyChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel mainChannel = new NotificationChannel("channel", "Main Channel", NotificationManager.IMPORTANCE_DEFAULT);
            mainChannel.setDescription("This chanel displays notification updates from the service");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(mainChannel);
        }
    }

    // inflating the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            SearchDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    View.OnClickListener fabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(searching){
                FloatingActionButton fab = findViewById(R.id.fab);
                fab.setImageResource(R.drawable.ic_add_white);
                fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#008477")));
                lvWorkouts.setAdapter(new RowIconAdapter(MainActivity.this, R.layout.row_main_workout, R.id.tvExerciseTitle, workoutNameList));
                searching = false;
            } else {
                AddWorkout();
            }
        }
    };

    AdapterView.OnItemClickListener listClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(MainActivity.this, ViewWorkout.class);
            intent.putExtra("WORKOUT", (String)parent.getItemAtPosition(position));
            startActivityForResult(intent, 0);
        }
    };


    // View holder for workout list
    public class workoutViewHolder {
        ImageView ivIcon;
        TextView tvWorkoutTitle;

        public workoutViewHolder(View view){
            ivIcon = view.findViewById(R.id.ivIcon);
            tvWorkoutTitle = view.findViewById(R.id.tvExerciseTitle);
        }
    }

    // display list of workouts
    class RowIconAdapter extends ArrayAdapter<String> {
        private ArrayList<String> workoutList;
        public RowIconAdapter(Context c, int rowResourceId, int textViewResourceId, ArrayList<String> items) {
            super(c, rowResourceId, textViewResourceId, items);
            workoutList  = items;
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            View row = convertView;
            workoutViewHolder holder = null;

            if(row == null){
                row = mInflater.inflate(R.layout.row_main_workout, parent, false);
                holder = new workoutViewHolder(row);
                row.setTag(holder);
            } else {
                holder = (workoutViewHolder)row.getTag();
            }

            String currWorkout = workoutList.get(pos);
            if (currWorkout != null) {
                holder.tvWorkoutTitle.setText(currWorkout);
                holder.ivIcon.setImageResource(R.drawable.ic_fitness_center_grey);
            }
            return row;
        }
    }

    // load in all the workouts saved to the device
    private void LoadFiles(){
        File directory = new File(getFilesDir().toString());
        File[] workoutFiles = directory.listFiles();
        FileInputStream iStream = null;
        workoutNameList.clear();
        workoutFileList.clear();
        for (int i = 0; i < workoutFiles.length; i++){
            workoutFileList.add(workoutFiles[i].getName());
            try {
                iStream = openFileInput(workoutFiles[i].getName());
                InputStreamReader streamReader = new InputStreamReader(iStream);
                BufferedReader bufferReader = new BufferedReader(streamReader);
                workoutNameList.add(bufferReader.readLine());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (iStream == null){
                    try{
                        iStream.close();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // displays search dialog
    private void SearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Search");

        final EditText etInput = new EditText(this);
        etInput.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(etInput);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                input = etInput.getText().toString();
                SearchList(input);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });

        builder.show();
    }

    // searches the list for name
    private void SearchList(String text) {
        ArrayList<String> searchResults = new ArrayList<>();
        if(text != ""){
            for(String str: workoutNameList) {
                if(str.contains(text)){
                    searchResults.add(str);
                }
            }
            if(!searchResults.isEmpty()) {
                lvWorkouts.setAdapter(new RowIconAdapter(MainActivity.this, R.layout.row_main_workout, R.id.tvExerciseTitle, searchResults));
                searching = true;
                FloatingActionButton fab = findViewById(R.id.fab);
                fab.setImageResource(R.drawable.ic_clear_white);
                fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EE0000")));
            } else {
                Toast.makeText(MainActivity.this, "No workout found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "No workout found", Toast.LENGTH_SHORT).show();
        }
    }

    private void AddWorkout(){
        Intent intent = new Intent(this, EditWorkout.class);
        intent.putExtra("ACTIVITY", "MainActivity");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(requestCode == 0) {
            if(resultCode == RESULT_OK){
                if(intent != null){
                    LoadFiles();
                    lvWorkouts.setAdapter(new RowIconAdapter(MainActivity.this, R.layout.row_main_workout, R.id.tvExerciseTitle, workoutNameList));
                    Log.d("MAIN", "success");
                } else {
                    Log.d("MAIN", "intent error");
                }
            } else {
                Log.d("MAIN", "result error");
            }
        } else {
            Log.d("MAIN", "request error");
        }
    }
}
