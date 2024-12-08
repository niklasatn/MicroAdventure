package com.example.microadventure;

public class ActivityHistory {
    private final String activityName;
    private final String date;

    public ActivityHistory(String activityName, String date) {
        this.activityName = activityName;
        this.date = date;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getDate() {
        return date;
    }
}
