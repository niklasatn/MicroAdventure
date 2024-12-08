package com.example.microadventure;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableFullscreenMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setupBottomNavigation();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        try (DatabaseHelper dBHelper = new DatabaseHelper(this)){
            HistoryActivityAdapter adapter = new HistoryActivityAdapter(dBHelper.getActivityHistory());
            recyclerView.setAdapter(adapter);
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
        bottomNavigationView.setSelectedItemId(R.id.nav_history);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            } else if (itemId == R.id.nav_more) {
                startActivity(new Intent(this, MoreActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
            return true;
        });
    }
}
