package com.example.microadventure;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MicroAdventure.db";
    @SuppressLint("SdCardPath")
    private static final String DATABASE_PATH = "/data/data/com.example.microadventure/databases/";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_FITNESS = "fitness_activities";
    private static final String TABLE_NATURE = "nature_activities";
    private static final String TABLE_SOCIAL = "social_activities";
    private static final String TABLE_ACTIVITY_HISTORY = "activity_history"; // Neue Tabelle
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        copyDatabaseFromAssets();
    }

    private void copyDatabaseFromAssets() {
        File databaseFile = new File(DATABASE_PATH + DATABASE_NAME);
        if (!databaseFile.exists()) {
            getReadableDatabase();
            try (InputStream input = context.getAssets().open("databases/" + DATABASE_NAME);
                 OutputStream output = Files.newOutputStream(databaseFile.toPath())) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                output.flush();

            } catch (IOException ignored) {}
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ACTIVITY_HISTORY_TABLE =
                "CREATE TABLE " + TABLE_ACTIVITY_HISTORY + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "activity TEXT NOT NULL, " +
                        "date TEXT NOT NULL);";
        db.execSQL(CREATE_ACTIVITY_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS activity_history");
        onCreate(db);
    }

    public String getRandomActivity() {
        List<String> allActivities = getAllActivities();
        return getRandomItem(allActivities);
    }

    private List<String> getAllActivities() {
        List<String> allActivities = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            addActivitiesFromTable(db, TABLE_FITNESS, allActivities);
            addActivitiesFromTable(db, TABLE_NATURE, allActivities);
            addActivitiesFromTable(db, TABLE_SOCIAL, allActivities);
        }
        return allActivities;
    }

    public List<ActivityHistory> getActivityHistory() {
        List<ActivityHistory> historyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT activity, date FROM activity_history", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int activityIndex = cursor.getColumnIndex("activity");
                    int dateIndex = cursor.getColumnIndex("date");

                    if (activityIndex != -1 && dateIndex != -1) {
                        String activity = cursor.getString(activityIndex);
                        String date = cursor.getString(dateIndex);
                        ActivityHistory activityHistory = new ActivityHistory(activity, date);
                        historyList.add(activityHistory);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return historyList;
    }


    private void addActivitiesFromTable(SQLiteDatabase db, String tableName, List<String> allActivities) {
        String query = "SELECT activity_content FROM " + tableName;
        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                allActivities.add(cursor.getString(0));
            }
        } catch (Exception ignored) {}
    }

    private String getRandomItem(List<String> list) {
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

    public void saveActivityToHistory(String activity) {
        SQLiteDatabase db = this.getWritableDatabase();
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        ContentValues values = new ContentValues();
        values.put("activity", activity);
        values.put("date", currentDateTime);

        db.insert(TABLE_ACTIVITY_HISTORY, null, values);
        db.close();
    }

    public void deleteLastActivityFromHistory() {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "DELETE FROM " + TABLE_ACTIVITY_HISTORY +
                " WHERE date = (SELECT MAX(date) FROM " + TABLE_ACTIVITY_HISTORY + ")";

        db.execSQL(query);
        db.close();
    }
}
