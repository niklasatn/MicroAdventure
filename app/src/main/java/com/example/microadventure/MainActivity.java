package com.example.microadventure;

import static android.view.View.INVISIBLE;
import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.os.Build;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Calendar;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_POST_NOTIFICATION = 1;
    private TextView activityTextView;
    private TextView NextUpdateTextView;
    private CardView activityCardView;
    private Button buttonAlternative;
    private Button buttonDone;
    private Button buttonHelp;
    public static SharedPreferences sharedPreferences;
    private boolean activityLoaded = false;
    private boolean loadAlternative = false;
    public static int notification_time = 8;
    KonfettiView konfettiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkAndroidVersion();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enableFullscreenMode();
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        activityTextView = findViewById(R.id.TextView_activity);
        NextUpdateTextView = findViewById(R.id.NextUpdate);
        activityCardView = findViewById(R.id.ActivityCardView);
        buttonAlternative = findViewById(R.id.buttonAlternative);
        buttonDone = findViewById(R.id.buttonDone);
        buttonHelp = findViewById(R.id.buttonHelp);
        konfettiView = findViewById(R.id.konfettiView);

        boolean isButtonAlternativeInvisible = sharedPreferences.getBoolean("buttonAlternative_invisible", false);
        boolean isButtonDoneInvisible = sharedPreferences.getBoolean("buttonDone_invisible", false);
        boolean isNextUpdateTextViewInvisible = sharedPreferences.getBoolean("NextUpdateTextView_invisible", true);
        boolean isviewsMovedDown = sharedPreferences.getBoolean("views_moved_down", false);
        boolean isactivityTextViewInvisible = sharedPreferences.getBoolean("activityTextView_invisible", false);
        boolean isactivityCardViewInvisible = sharedPreferences.getBoolean("activityCardView_invisible", false);
        boolean isIntroFinished = sharedPreferences.getBoolean("Intro_Finished", false);

        if(!isIntroFinished){
            Intent intent = new Intent(getApplicationContext(), IntroActivity.class);
            startActivity(intent);
        }
        if (isButtonAlternativeInvisible) {
            buttonAlternative.setVisibility(INVISIBLE);
        }
        if (isButtonDoneInvisible) {
            buttonDone.setVisibility(INVISIBLE);
            buttonAlternative.setVisibility(INVISIBLE);
            NextUpdateTextView.setVisibility(View.VISIBLE);
            calculateTimeUntilNextUpdate();
        }
        if(isviewsMovedDown){
            animateViewsDown();
        }
        if(!isNextUpdateTextViewInvisible){
            NextUpdateTextView.setVisibility(INVISIBLE);
        }
        if(isactivityTextViewInvisible){
            activityTextView.setVisibility(View.VISIBLE);
        }
        if(isactivityCardViewInvisible){
            activityCardView.setVisibility(View.VISIBLE);
        }

        setupBottomNavigation();
        handlePermissions();
        loadSavedActivity();
        scheduleDailyActivityUpdate();

        buttonHelp.setOnClickListener(view -> {
            Intent intent2 = new Intent(getApplicationContext(), IntroActivity.class);
            startActivity(intent2);
        });

        buttonAlternative.setOnClickListener(view -> {
            buttonAlternative.setEnabled(false);
            loadAlternative = true;
            updateActivity(true);

            buttonAlternative.setVisibility(INVISIBLE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("buttonAlternative_invisible", true);
            editor.apply();
        });

        buttonDone.setOnClickListener(view -> {
            buttonDone.setEnabled(false);
            buttonDone.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_green_dark));
            setLayoutDone();
        });
    }

    private void enableFullscreenMode() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View mainContainer = findViewById(R.id.main_layout);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), mainContainer);
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (itemId == R.id.nav_more) {
                startActivity(new Intent(this, MoreActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
            return true;
        });
    }

    private void scheduleDailyActivityUpdate() {
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        int lastUpdatedDay = preferences.getInt("lastUpdatedDay", -1);
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        if (lastUpdatedDay != currentDay) {
            updateActivity(false);
            preferences.edit().putInt("lastUpdatedDay", currentDay).apply();

            Button buttonAlternative = findViewById(R.id.buttonAlternative);
            Button buttonDone = findViewById(R.id.buttonDone);

            buttonAlternative.setEnabled(true);
            buttonAlternative.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));

            buttonDone.setEnabled(true);
            buttonDone.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

        new Handler().postDelayed(this::scheduleDailyActivityUpdate, delay);
    }

    private void updateActivity(boolean loadAlternative) {
        this.loadAlternative = loadAlternative;
        try (DatabaseHelper dbHelper = new DatabaseHelper(this)) {
            String randomActivity = dbHelper.getRandomActivity();
            if (randomActivity != null && !loadAlternative) {
                activityTextView.setText(randomActivity);
                saveActivityToPreferences(randomActivity);
                dbHelper.saveActivityToHistory(randomActivity);
                activityLoaded = true;
                resetLayout();
            } else if(randomActivity != null){
                activityTextView.setText(randomActivity);
                saveActivityToPreferences(randomActivity);
                dbHelper.deleteLastActivityFromHistory();
                dbHelper.saveActivityToHistory(randomActivity);
                activityLoaded = true;
            }
        }
    }

    private void saveActivityToPreferences(String activity) {
        SharedPreferences sharedPreferences = getSharedPreferences("MicroAdventurePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedActivity", activity);
        editor.apply();
    }

    private void loadSavedActivity() {
        SharedPreferences sharedPreferences = getSharedPreferences("MicroAdventurePrefs", MODE_PRIVATE);
        String savedActivity = sharedPreferences.getString("savedActivity", null);
        if (savedActivity != null) {
            activityTextView.setText(savedActivity);
            activityLoaded = true;
        }
    }

    private void calculateTimeUntilNextUpdate() {
        Calendar currentTime = Calendar.getInstance();

     Calendar nextUpdateTime = Calendar.getInstance();
        nextUpdateTime.set(Calendar.HOUR_OF_DAY, 0);
        nextUpdateTime.set(Calendar.MINUTE, 0);
        nextUpdateTime.set(Calendar.SECOND, 0);
        nextUpdateTime.set(Calendar.MILLISECOND, 0);

        if (currentTime.after(nextUpdateTime)) {
            nextUpdateTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        long timeDifferenceMillis = nextUpdateTime.getTimeInMillis() - currentTime.getTimeInMillis();

        long hours = timeDifferenceMillis / (1000 * 60 * 60);
        long minutes = (timeDifferenceMillis / (1000 * 60)) % 60;
        long seconds = (timeDifferenceMillis / 1000) % 60;

        String timeUntilUpdate = String.format("Für heute ist alles erledigt! \nNächste Aktivität in: %02d:%02d:%02d", hours, minutes, seconds);
        NextUpdateTextView.setText(timeUntilUpdate);
        NextUpdateTextView.setVisibility(View.VISIBLE);

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("TextView_activity_invisible", true);
        editor.putBoolean("nextUpdateTextView_visible", true);
        editor.apply();

        new Handler().postDelayed(this::calculateTimeUntilNextUpdate, 1000);
    }

    public void BuildKonfetti(){
        konfettiView.build()
                .addColors(
                        Color.parseColor("#88C8FF"),
                        Color.parseColor("#58B1FF"),
                        Color.parseColor("#708EC5")
                )
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 10f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new Size(12, 5f))
                .setPosition(50f, konfettiView.getWidth() + 50f, -50f, -50f)
                .streamFor(300, 2000L);
    }

    private void animateViewsDown() {
        TranslateAnimation animateCardView = new TranslateAnimation(0, 0, 0, 600);
        animateCardView.setDuration(500);
        animateCardView.setFillAfter(true);
        activityCardView.startAnimation(animateCardView);
        activityCardView.setCardBackgroundColor(getColor(R.color.gray));
    }

    private void animateViewsUp() {
        TranslateAnimation animateCardView = new TranslateAnimation(0, 0, 600, 0);
        animateCardView.setDuration(500);
        animateCardView.setFillAfter(true);
        activityCardView.startAnimation(animateCardView);
        activityCardView.setCardBackgroundColor(getColor(R.color.white));
    }

    private void resetLayout(){
        buttonDone.setVisibility(View.VISIBLE);
        buttonAlternative.setVisibility(View.VISIBLE);
        activityCardView.setVisibility(View.VISIBLE);
        activityTextView.setVisibility(View.VISIBLE);
        NextUpdateTextView.setVisibility(INVISIBLE);

        animateViewsUp();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("buttonDone_invisible", false);
        editor.putBoolean("buttonAlternative_invisible", false);
        editor.putBoolean("ActivityCardView_invisible", false);
        editor.putBoolean("views_moved_down", false);
        editor.putBoolean("NextUpdateTextView_invisible", true);
        editor.putBoolean("activityTextView_invisible", false);
        editor.putBoolean("activityCardView_invisible", false);
        editor.apply();
    }

    private void setLayoutDone(){
        new Handler().postDelayed(() -> {
            buttonDone.setVisibility(View.INVISIBLE);
            buttonAlternative.setVisibility(INVISIBLE);
            NextUpdateTextView.setVisibility(View.VISIBLE);
            activityTextView.setVisibility(View.VISIBLE);
            activityCardView.setCardBackgroundColor(getColor(R.color.gray));

            animateViewsDown();
            calculateTimeUntilNextUpdate();
            BuildKonfetti();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("buttonDone_invisible", true);
            editor.putBoolean("buttonAlternative_invisible", true);
            editor.putBoolean("views_moved_down", true);
            editor.putBoolean("NextUpdateTextView_invisible", false);
            editor.putBoolean("activityTextView_invisible", false);
            editor.putBoolean("activityCardView_invisible", false);
            editor.apply();
        }, 500);
    }

    public void handlePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATION);
        } else {
            scheduleNotification(this, notification_time);
        }
    }

    public static void scheduleNotification(Context context, int notification_time) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, notification_time);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    public static void cancelNotification(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scheduleNotification(this, notification_time);
        }
    }

    private void checkAndroidVersion() {
        int currentApiVersion = Build.VERSION.SDK_INT;
        int minApiVersion = 30;

        if (currentApiVersion < minApiVersion) {
            Toast.makeText(this, "Deine Android Version ist zu alt und wird nicht unterstützt. Bitte aktualisiere dein Gerät", Toast.LENGTH_LONG).show();
        }
    }
}