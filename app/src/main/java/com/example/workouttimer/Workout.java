package com.example.workouttimer;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class Workout implements Parcelable {

    private String fileName, name;
    private int time, calories;
    private ArrayList<Exercise> exerciseList;

    public Workout(String aFileName, String aName, int aTime, int aCalories, ArrayList<Exercise> aList){
        fileName = aFileName;
        name = aName;
        time = aTime;
        calories = aCalories;
        exerciseList = aList;
    }

    protected Workout(Parcel in) {
        fileName = in.readString();
        name = in.readString();
        time = in.readInt();
        calories = in.readInt();
        exerciseList = in.createTypedArrayList(Exercise.CREATOR);
    }

    public static final Creator<Workout> CREATOR = new Creator<Workout>() {
        @Override
        public Workout createFromParcel(Parcel in) {
            return new Workout(in);
        }

        @Override
        public Workout[] newArray(int size) {
            return new Workout[size];
        }
    };

    public void Add(Exercise newExercise, int index){
        if(index != -1){
            Log.d("TEST", "overwriting");
            exerciseList.set(index, newExercise);
        }
        else{
            Log.d("TEST", "adding new");
            exerciseList.add(newExercise);
        }
    }

    public void Remove(int index, Context context){
        if(index != -1){
            Log.d("TEST", "removing " + index);
            exerciseList.remove(index);
        }
        else{
            Toast.makeText(context, "Item cannot be removed", Toast.LENGTH_SHORT).show();
        }
    }

    public String getFileName(){
        return fileName;
    }

    public String getName() {
        return name;
    }

    public int getTime() {
        return time;
    }

    public int getCalories() {
        return calories;
    }

    public ArrayList<Exercise> getExerciseList() {
        return exerciseList;
    }

    public Exercise getExerciseListItem(int pos) {
        return exerciseList.get(pos);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileName);
        dest.writeString(name);
        dest.writeInt(time);
        dest.writeInt(calories);
        dest.writeTypedList(exerciseList);
    }
}
