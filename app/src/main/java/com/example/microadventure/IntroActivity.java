package com.example.microadventure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntro2;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.AppIntroPageTransformerType;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance("Entdecke das Besondere im Kleinen.", "Mit MicroAdventure findest du jeden Tag eine neue, kleine Herausforderung oder Aktivität, die deinen Alltag aufpeppt. " +
                "Ob ein Spaziergang in der Natur, ein kreativer DIY-Workshop oder ein kulinarisches Experiment – lass dich inspirieren und erlebe kleine Abenteuer direkt vor deiner Haustür!" +
                "\nGroße Erlebnisse beginnen mit kleinen Schritten.", R.drawable.microadventure, getColor(R.color.light_green), getColor(R.color.black), getColor(R.color.black)));

        addSlide(AppIntroFragment.newInstance("Abenteuer für jeden Moment", " Egal ob alleine, mit Freunden oder der Familie – finde Aktivitäten, die perfekt zu dir passen.", R.drawable.family, getColor(R.color.light_yellow), getColor(R.color.black), getColor(R.color.black)));

        addSlide(AppIntroFragment.newInstance("Einleitung abgeschlossen", "Drücke auf Fertig, um mit deinem ersten Abenteuer zu beginnen!", R.drawable.ending, getColor(R.color.light_blue), getColor(R.color.black), getColor(R.color.black)));

        setSkipButtonEnabled(false);
        setIndicatorEnabled(true);
        setTransformer(AppIntroPageTransformerType.Depth.INSTANCE);
        setColorTransitionsEnabled(true);
        setSystemBackButtonLocked(false);
    }

    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        editor.putBoolean("Intro_Finished", true);
        editor.apply();
    }
}
