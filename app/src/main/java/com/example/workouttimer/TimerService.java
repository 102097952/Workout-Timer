package com.example.workouttimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class TimerService extends Service {

    private IBinder mBinder = new ServiceBinder();
    private Handler mHandler;
    private int mProgress, mMaxValue, mCurrIndex;
    private Boolean mIsPaused;
    private String name;
    private NotificationManager notificationManager;
    private ArrayList<String> mCheckPointNames;
    private ArrayList<Integer> mCheckPointTimes;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        mProgress = 0;
        mCurrIndex = 0;
        mIsPaused = true;

        // set notification objects
        notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        registerReceiver(broadcastReceiver, new IntentFilter("NEXT"));
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if(intent.getExtras() != null){
            mCheckPointNames = intent.getStringArrayListExtra("NAMES");
            mCheckPointTimes = intent.getIntegerArrayListExtra("TIMES");
            mMaxValue = intent.getIntExtra("TOTALTIME", 1000);
            name = intent.getStringExtra("WORKOUTNAME");

            if (mMaxValue < 1000){
                mMaxValue = 1000;
            }
            Log.d("TIMER", "obtained");
        }
        else {
            Log.d("TIMER", "not found");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ServiceBinder extends Binder {
        TimerService getService(){
            return TimerService.this;
        }
    }

    // workout timer loop
    public void runWorkout(){
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(mProgress >= mMaxValue ){
                    // workout is complete
                    Log.d("TIMER", "Workout complete! Well done!");
                    sendCompleteNotification(name);
                    mHandler.removeCallbacks(this);
                    pauseWorkout();
                }
                else if(mIsPaused){
                    // workout is paused
                    Log.d("TIMER", "Paused");
                    mHandler.removeCallbacks(this);
                    pauseWorkout();
                }
                else if (mProgress >= mCheckPointTimes.get(mCurrIndex)) {
                    // current exercise is complete, wait to start next exercise
                    sendProgressNotification(name, mCheckPointNames.get(mCurrIndex) + " is complete!");
                    Log.d("TIMER", mCheckPointNames.get(mCurrIndex) + " finished");
                    mCurrIndex++;
                    pauseWorkout();
                } else {
                    // workout is currently underway
                    mProgress += 100;
                    Log.d("TIMER", "Progress: "+ mProgress);
                    mHandler.postDelayed(this, 100);
                }
            }
        };
        mHandler.postDelayed(runnable, 100);
    }

    public void pauseWorkout() {
        mIsPaused = true;
    }

    public void unPauseWorkout(){
        mIsPaused = false;
        runWorkout();
    }

    public Boolean getIsPaused() {
        return mIsPaused;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public int getProgress() {
        return mProgress;
    }

    public void resetTask(){
        mProgress = 0;
        mCurrIndex = 0;
    }

    public String getCurrExercise(){
        if(mCurrIndex < mCheckPointTimes.size()){
            return mCheckPointNames.get(mCurrIndex);
        }
        else {
            return "";
        }
    }

    public String getNextExercise(){
        if(mCurrIndex + 1 < mCheckPointTimes.size()){
            return mCheckPointNames.get(mCurrIndex + 1);
        }
        else {
            return "";
        }
    }

    public void startNext(){
        Log.d("TIMER", "startNext called");
        if(!getIsPaused()){
            mProgress = mCheckPointTimes.get(mCurrIndex);
            mCurrIndex++;
        }
        unPauseWorkout();
        notificationManager.cancelAll();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }


    // notifications
    public void sendProgressNotification(String title, String text){
        Intent activityIntent = new Intent(this, ViewWorkout.class);
        activityIntent.setAction(Intent.ACTION_MAIN);
        activityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);

        Intent broadcastIntent = new Intent(this, NotificationReceiver.class);
        broadcastIntent.putExtra("message", "testing");
        PendingIntent actionIntent = PendingIntent.getBroadcast(this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, "channel")
                .setSmallIcon(R.drawable.ic_fitness_center_grey)
                .setContentTitle(title)
                .setContentText(text)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher, "START NEXT EXERCISE", actionIntent)
                .build();

        notificationManager.notify(1, notification);
    }

    public void sendCompleteNotification(String title){
        Intent activityIntent = new Intent(this, MainActivity
                .class);
        activityIntent.setAction(Intent.ACTION_MAIN);
        activityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, "channel")
                .setSmallIcon(R.drawable.ic_fitness_center_grey)
                .setContentTitle(title)
                .setContentText("Workout complete! Well done!")
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(2, notification);
    }

    // resume workout from notification prompt
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TIMER", "recived");
            notificationManager.cancelAll();
            startNext();
        }
    };

    @Override
    public boolean onUnbind(Intent intent) {
        notificationManager.cancel(1);
        return super.onUnbind(intent);
    }
}
