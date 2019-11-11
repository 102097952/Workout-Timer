package com.example.workouttimer;

import android.os.Parcel;
import android.os.Parcelable;

public class Exercise implements Parcelable {

    private String name;
    private int breakTime, timer, weight, reps, sets, distance;

    public Exercise(String aName, int aBreakTime, int aTimer, int aWeight, int aReps, int aSets, int aDistance){
        name = aName;
        breakTime = aBreakTime;
        timer = aTimer;
        weight = aWeight;
        reps = aReps;
        sets = aSets;
        distance = aDistance;
    }

    protected Exercise(Parcel in) {
        name = in.readString();
        breakTime = in.readInt();
        timer = in.readInt();
        weight = in.readInt();
        reps = in.readInt();
        sets = in.readInt();
        distance = in.readInt();
    }

    public static final Creator<Exercise> CREATOR = new Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };

    public String getName() {
        return name;
    }

    public int getBreakTime() {
        return breakTime;
    }

    public int getTimer() {
        return timer;
    }

    public int getWeight() {
        return weight;
    }

    public int getReps() {
        return reps;
    }

    public int getSets() {
        return sets;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(breakTime);
        dest.writeInt(timer);
        dest.writeInt(weight);
        dest.writeInt(reps);
        dest.writeInt(sets);
        dest.writeInt(distance);
    }
}
