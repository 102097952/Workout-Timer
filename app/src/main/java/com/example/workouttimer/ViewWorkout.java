package com.example.workouttimer;

import android.app.Activity;
import android.app.Notification;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
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

public class ViewWorkout extends AppCompatActivity {

    private Workout workout;
    private LayoutInflater mInflater;
    private ListView lvExercises;
    private ViewPager vpContainer;
    private MutableLiveData<Boolean> mIsProgressUpdating = new MutableLiveData<>();
    private MutableLiveData<TimerService.ServiceBinder> mBinder = new MutableLiveData<>();
    private TimerService mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_workout);

        // find views
        Button btnBack = findViewById(R.id.btnBack);
        Button btnStart = findViewById(R.id.btnStart);

        vpContainer = findViewById(R.id.vpContainer);
        lvExercises = findViewById(R.id.lvExercises);

        // set onClickListeners
        btnBack.setOnClickListener(backClickListener);
        btnStart.setOnClickListener(startClickListener);

        // get data from file
        LoadFromFile(getIntent().getStringExtra("WORKOUT"));

        // inflate list view
        mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        lvExercises.setAdapter(new ViewWorkout.RowIconAdapter(ViewWorkout.this,
                R.layout.row_view_workout, R.id.tvExerciseTitle, workout.getExerciseList()));

        setUpViewPager(vpContainer);
        vpContainer.setCurrentItem(0);

        setObservers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_workout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            stopTimer();
            EditWorkout();
            return true;
        }
        else if (id == R.id.action_delete) {
            DeleteMessage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // sets up fragments
    private void setUpViewPager(ViewPager viewPager){
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        FragWorkoutProgress fragWorkoutProgress = new FragWorkoutProgress();
        FragWorkoutInfo fragWorkoutInfo = new FragWorkoutInfo();

        Bundle bundle = new Bundle();
        bundle.putString("WORKOUT", workout.getName());
        bundle.putInt("TIME", workout.getTime());

        fragWorkoutProgress.setArguments(bundle);
        fragWorkoutInfo.setArguments(bundle);

        adapter.addFragment(fragWorkoutProgress, "Workout Progress");
        adapter.addFragment(fragWorkoutInfo, "Workout Information");
        viewPager.setAdapter(adapter);
    }

    // go back to Main Activity
    View.OnClickListener backClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mService != null){
                if(mIsProgressUpdating.getValue()){
                    stopTimer();
                    Toast.makeText(ViewWorkout.this, "Timer Canceled", Toast.LENGTH_SHORT).show();
                }
            }
            Intent newIntent = new Intent(ViewWorkout.this, MainActivity.class);
            setResult(Activity.RESULT_OK, newIntent);
            finish();
        }
    };

    // start exercise
    View.OnClickListener startClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleUpdate();
        }
    };


    // creates the list
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
            workoutList = items;
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewWorkout.exerciseViewHolder holder = null;

            if(row == null){
                row = mInflater.inflate(R.layout.row_view_workout, parent, false);
                holder = new ViewWorkout.exerciseViewHolder(row);
                row.setTag(holder);
            } else {
                holder = (ViewWorkout.exerciseViewHolder)row.getTag();
            }

            if (workoutList.get(pos) != null) {
                String currWorkout = workoutList.get(pos).getName();
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


    // loads workout object from file
    private void LoadFromFile(String inputWorkoutName){
        ArrayList<Exercise> newList = new ArrayList<>();
        String filename = "w"+inputWorkoutName.replaceAll("\\s+", "")+".txt";
        FileInputStream iStream = null;
        String workoutName = "";

        try{
            iStream = openFileInput(filename);
            InputStreamReader streamReader = new InputStreamReader(iStream);
            BufferedReader bufferReader = new BufferedReader(streamReader);
            String text;

            workoutName = bufferReader.readLine();

            while((text = bufferReader.readLine()) != null){
                String[] split = text.split(",");
                newList.add(new Exercise(split[0], Integer.parseInt(split[1]),
                        Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]),
                        Integer.parseInt(split[5]), Integer.parseInt(split[6])));
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e){
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
        int time = getTotalTime(newList);

        workout = new Workout(filename, workoutName, time, 1000, newList);
        int timeMins = time/60000;
        Log.d("VIEW", timeMins+"Mins");
    }

    private int getTotalTime(ArrayList<Exercise> exerciseList) {
        int totalTime = 0;
        for(Exercise exercise: exerciseList){
            for(int i = 0; i < exercise.getSets(); i++){
                totalTime += exercise.getTimer();
                totalTime += exercise.getBreakTime();
            }
        }
        return totalTime * 60000;
    }

    private void EditWorkout(){
        Intent intent = new Intent(this, EditWorkout.class);
        intent.putExtra("WORKOUT", workout);
        intent.putExtra("ACTIVITY", "ViewWorkout");
        startActivityForResult(intent, 0);
    }

    private void DeleteMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewWorkout.this);
        builder.setMessage("Do you want to delete? This action cannot be undone.");
        builder.setTitle("Alert");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(DeleteWorkout(workout.getFileName())){
                    Toast.makeText(ViewWorkout.this, "File deleted", Toast.LENGTH_SHORT).show();
                    Log.d("TEST", "Deleted");
                    Intent intent = new Intent(ViewWorkout.this, MainActivity.class);
                    setResult(Activity.RESULT_OK, intent);
                    stopTimer();
                    finish();
                }
                else {
                    Toast.makeText(ViewWorkout.this, "No file found", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ViewWorkout.this, "Canceled", Toast.LENGTH_SHORT).show();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(requestCode == 0 && resultCode == RESULT_OK  && intent != null) {
            // check if file has been deleted
            if(intent.getBooleanExtra("FILEOK", false)){
                LoadFromFile(intent.getStringExtra("WORKOUT"));
                setUpViewPager(vpContainer);
                lvExercises.setAdapter(new ViewWorkout.RowIconAdapter(ViewWorkout.this,
                        R.layout.row_view_workout, R.id.tvExerciseTitle, workout.getExerciseList()));
            } else {
                // file has been deleted
                Intent newIntent = new Intent(ViewWorkout.this, MainActivity.class);
                setResult(Activity.RESULT_OK, newIntent);
                finish();
            }
        } else {
            Log.d("MAIN", "result error");
        }
    }


    // service code
    private void setObservers(){

        mBinder.observe(this, new Observer<TimerService.ServiceBinder>(){
            @Override
            public void onChanged(@Nullable TimerService.ServiceBinder serviceBinder) {
                if(serviceBinder != null){
                    Log.d("VIEW", "Connected to service");
                    mService = serviceBinder.getService();
                }
                else {
                    Log.d("VIEW", "Unbound from service");
                    mService = null;
                }
            }
        });

        mIsProgressUpdating.setValue(false);
        mIsProgressUpdating.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean aBoolean) {
                Button btnStart = findViewById(R.id.btnStart);
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(mIsProgressUpdating.getValue()) {
                            if(mBinder.getValue() != null){
                                if(mService.getProgress() == mService.getMaxValue()){
                                    mIsProgressUpdating.postValue(false);
                                }
                                ProgressBar pbTotalWorkout = findViewById(R.id.pbTotalWorkout);
                                TextView tvExercise = findViewById(R.id.tvExercise);
                                TextView tvNext = findViewById(R.id.tvNext);
                                Button btnNext = findViewById(R.id.btnNext);

                                pbTotalWorkout.setProgress(mService.getProgress());
                                pbTotalWorkout.setMax(mService.getMaxValue());
                                tvExercise.setText(mService.getCurrExercise());
                                tvNext.setText("Next: " + mService.getNextExercise());
                                btnNext.setText("NEXT");
                                Log.d("VIEW", "next");
                            }
                            handler.postDelayed(this, 100);
                        }
                        else {
                            Button btnNext = findViewById(R.id.btnNext);
                            btnNext.setText("START");
                            Log.d("VIEW", "start");
                            handler.removeCallbacks(this);
                        }
                    }
                };

                if(aBoolean) {
                    btnStart.setText("STOP");
                    vpContainer.setCurrentItem(1);
                    handler.postDelayed(runnable, 100);
                }
                else {
                    btnStart.setText("START");
                    vpContainer.setCurrentItem(0);
                }
            }
        });
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("VIEW", "connected");
            TimerService.ServiceBinder binder = (TimerService.ServiceBinder) service;
            mBinder.postValue(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("VIEW", "disconnected");
            mBinder.postValue(null);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        startService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBinder != null){
            unbindService(serviceConnection);
        }
    }

    private void startService() {
        ArrayList<Integer> checkpointTimes = new ArrayList<>();
        ArrayList<String> checkpointNames = new ArrayList<>();
        int timerCount = 0;
        String displayText;

        for(int i = 0; i < workout.getExerciseList().size(); i++){
            Exercise currExercise = workout.getExerciseListItem(i);
            for(int j = 0; j < currExercise.getSets(); j++){
                // checkpoints for exercises
                timerCount += currExercise.getTimer() * 60000;
                displayText = currExercise.getName();
                if(currExercise.getSets() > 1){
                    int setNo = j + 1;
                    displayText += " | Set " + setNo;
                }
                checkpointTimes.add(timerCount);
                checkpointNames.add(displayText);

                // checkpoints for breaks
                timerCount += currExercise.getBreakTime() * 60000;
                displayText = "Break";
                if(currExercise.getSets() > 1){
                    int setNo = j + 1;
                    displayText += " | Set " + setNo;
                }
                checkpointTimes.add(timerCount);
                checkpointNames.add(displayText);
            }
        }

        Intent serviceIntent = new Intent(this, TimerService.class);
        serviceIntent.putExtra("NAMES", checkpointNames);
        serviceIntent.putExtra("TIMES", checkpointTimes);
        serviceIntent.putExtra("TOTALTIME", timerCount);
        serviceIntent.putExtra("WORKOUTNAME", workout.getName());
        startService(serviceIntent);
        bindService();
        Log.d("VIEW", "Started");
    }

    private void bindService() {
        Intent serviceIntent = new Intent(this, TimerService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void toggleUpdate(){
        Button btnStart = findViewById(R.id.btnStart);
        if(mService != null) {
            if (mService.getIsPaused()){
                mService.resetTask();
                mService.unPauseWorkout();
                mIsProgressUpdating.postValue(true);
                Log.d("VIEW", "starting");
            }
            else {
                mService.pauseWorkout();
                mService.resetTask();
                mIsProgressUpdating.postValue(false);
                Log.d("VIEW", "stopping");
            }
        }
        else {
            btnStart.setText("STOP");
            vpContainer.setCurrentItem(1);
            Log.d("VIEW", "cancelling");
        }
    }

    private void stopTimer() {
        mService.pauseWorkout();
        mService.resetTask();
        mIsProgressUpdating.postValue(false);
    }

    public void startNext(){
        if(mService != null){
            mService.startNext();
        }
    }
}
