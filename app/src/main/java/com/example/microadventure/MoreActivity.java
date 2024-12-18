package com.example.microadventure;

import static com.example.microadventure.ApkDownloader.getCurrentAppVersion;
import static com.example.microadventure.ApkDownloader.isNewVersionAvailable;
import static com.example.microadventure.MainActivity.notification_time;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Context;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.io.IOException;

public class MoreActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AppPrefs";
    private static final String NOTIFICATION_TIME_KEY = "notification_time";
    private static final String SWITCH_STATUS_KEY = "notification_switch_status";
    private static final int REQUEST_CODE_INSTALL_PERMISSION = 1002;
    private ActivityResultLauncher<Intent> installPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableFullscreenMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        setupBottomNavigation();

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        notification_time = sharedPreferences.getInt(NOTIFICATION_TIME_KEY, 8);

        Button buttonReset = findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(v -> showResetDialog(this));

        Button buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonUpdate.setOnClickListener(v -> handlePermissions());

        Slider notificationSlider = findViewById(R.id.notification_slider);
        notificationSlider.setValue(notification_time);

        notificationSlider.addOnChangeListener((slider, value, fromUser) -> {
            notification_time = (int) value;
            saveNotificationTime(notification_time);
            MainActivity.scheduleNotification(this, notification_time);
        });

        boolean[] switchState = getSwitchState();
        boolean isSwitchChecked = switchState[0];

        SwitchMaterial notificationSwitch = findViewById(R.id.notification_switch);
        notificationSwitch.setChecked(isSwitchChecked);
        updateSwitchTrackTint(notificationSwitch, isSwitchChecked);

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                notificationSwitch.setTrackTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
                MainActivity.scheduleNotification(this, notification_time);
                Toast.makeText(this, "Benachrichtigung eingeschaltet", Toast.LENGTH_SHORT).show();
            } else {
                notificationSwitch.setTrackTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
                MainActivity.cancelNotification(this);
                Toast.makeText(this, "Benachrichtigung ausgeschaltet", Toast.LENGTH_SHORT).show();
            }
            saveSwitchState(isChecked, notificationSwitch.isEnabled());
        });
    }

    private void updateSwitchTrackTint(SwitchMaterial notificationSwitch, boolean isChecked) {
        if (isChecked) {
            notificationSwitch.setTrackTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
        } else {
            notificationSwitch.setTrackTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        }
    }

    private void saveNotificationTime(int time) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(NOTIFICATION_TIME_KEY, time);
        editor.apply();
    }

    private void saveSwitchState(boolean isChecked, boolean isEnabled) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SWITCH_STATUS_KEY + "_checked", isChecked);
        editor.putBoolean(SWITCH_STATUS_KEY + "_enabled", isEnabled);
        editor.apply();
    }

    private boolean[] getSwitchState() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isChecked = sharedPreferences.getBoolean(SWITCH_STATUS_KEY + "_checked", true); // Standardwert: true (eingeschaltet)
        boolean isEnabled = sharedPreferences.getBoolean(SWITCH_STATUS_KEY + "_enabled", true); // Standardwert: true (aktiviert)
        return new boolean[]{isChecked, isEnabled};
    }

    public void showResetDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("App zurücksetzen")
                .setMessage("Möchtest du wirklich die App zurücksetzen?\nDies kann nicht rückgängig gemacht werden!")
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("Ja", (dialog, which) -> {
                    new AlertDialog.Builder(context)
                            .setTitle("Sicher?")
                            .setMessage("Wirklich sicher, dass du alle Abenteuer unwiderruflich löschen möchtest?")
                            .setNegativeButton("Nein", null)
                            .setPositiveButton("Ja", (confirmDialog, confirmWhich) -> resetAppData(context))
                            .show();
                })
                .show();
    }

    public void resetAppData(Context context) {
        try {
            String packageName = context.getPackageName();
            Runtime.getRuntime().exec("pm clear " + packageName);

            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            Runtime.getRuntime().exit(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handlePermissions() {
        installPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        onPermissionGranted();
                    } else {
                        Toast.makeText(MoreActivity.this, "Berechtigung abgelehnt", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        if (!getPackageManager().canRequestPackageInstalls()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + getPackageName()));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Falls der Intent auf dem Gerät nicht unterstützt wird
                Toast.makeText(this, "Einstellungen können nicht geöffnet werden", Toast.LENGTH_SHORT).show();
            }
        } else {
            //onPermissionGranted();
        }
    }

    private void onPermissionGranted() {
        String githubApkUrl = "https://github.com/niklasatn/MicroAdventure/raw/bcb69b59bfce778474143cf2cc50c853008196a8/MicroAdventure.apk";
        new ApkDownloader(this).execute(githubApkUrl);

        String apkPath = "path_to_downloaded_apk";
        String apkVersion = ApkDownloader.getApkVersion(apkPath);
        String currentAppVersion = getCurrentAppVersion();

        if (isNewVersionAvailable(apkVersion, currentAppVersion)) {
            Toast.makeText(this,"Neue Version wird heruntergeladen und installiert...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,"Version ist aktuell", Toast.LENGTH_SHORT).show();
        }
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
        bottomNavigationView.setSelectedItemId(R.id.nav_more);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
            return true;
        });
    }
}
