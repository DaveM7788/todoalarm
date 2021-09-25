package com.davesprojects.dm.alarm.ui;

import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;

import com.davesprojects.dm.alarm.R;


/**
 * Created by blue on 12/30/2017.
 */

public class StopwatchFrag extends Fragment implements View.OnClickListener {
    Context con;
    View myView;

    Chronometer timerChrono;
    Button buttonTimer, buttonResumeTimer;

    boolean chronoOn;
    boolean paused;
    private long lastPause;
    boolean wasRestored = false;

    long restoreTime = 0;

    CharSequence pauseText;

    // SystemClock.elapsedRealtime = returns time since system was booted
    // chrono.getBase   = return the base time as set by setBase(long)


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.stopwatch, container, false);
        con = myView.getContext();

        // for back stack
        SharedPreferences.Editor prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit();
        prefEditor.putString("lastFrag", "StopwatchFrag");
        prefEditor.apply();

        timerChrono = myView.findViewById(R.id.stopWatchChrono);
        buttonTimer = myView.findViewById(R.id.buttonStopwatch);
        buttonTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstButtonStopWatch();
            }
        });

        buttonResumeTimer = myView.findViewById(R.id.buttonResumeStopWatch);
        buttonResumeTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartStopWatch();
            }
        });

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("CHRONO")) {
                restoreTime = savedInstanceState.getLong("CHRONO");
                paused = savedInstanceState.getBoolean("CHRONOPAUSED");
                wasRestored = true;
                if (!paused) {
                    chronoOn = true;
                    buttonTimer.setText("Pause");
                    timerChrono.setBase(restoreTime);
                    timerChrono.start();
                }
                else {
                    lastPause = savedInstanceState.getLong("LASTPAUSE");
                    pauseText = savedInstanceState.getCharSequence("CHRONOTEXT");
                    timerChrono.setText(pauseText);
                    buttonTimer.setText("Resume");
                }
            }
        }

        return myView;
    }

    @Override
    public void onClick(View view) {
        // required override
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // handle screen rotation (which normally resets chrono = bad!)
        outState.putLong("CHRONO", timerChrono.getBase());
        outState.putBoolean("CHRONOPAUSED", paused);
        outState.putLong("LASTPAUSE", lastPause);
        outState.putCharSequence("CHRONOTEXT", timerChrono.getText());
    }

    public void firstButtonStopWatch() {
        if (!chronoOn) {
            if (!paused) {
                startStopWatch();
            } else {
                resumeStopWatch();
            }
        } else {
            pauseStopWatch();
        }
    }

    public void startStopWatch() {
        chronoOn = true;
        buttonTimer.setText("Pause");
        timerChrono.setBase(SystemClock.elapsedRealtime());
        timerChrono.start();
    }

    public void restartStopWatch() {
        chronoOn = true;
        paused = false;
        wasRestored = false;
        timerChrono.setBase(SystemClock.elapsedRealtime());
        timerChrono.start();
        buttonTimer.setText("Pause");
    }

    public void pauseStopWatch() {
        buttonTimer.setText("Resume");
        lastPause = SystemClock.elapsedRealtime();
        timerChrono.stop();
        paused = true;
        chronoOn = false;
    }

    public void resumeStopWatch() {
        buttonTimer.setText("Pause");
        chronoOn = true;
        paused = false;
        if (!wasRestored) {
            timerChrono.setBase(timerChrono.getBase() + SystemClock.elapsedRealtime() - lastPause);
        }
        else {
            timerChrono.setBase(restoreTime + SystemClock.elapsedRealtime() - lastPause);
            wasRestored = false;
        }

        timerChrono.start();
    }

    // screen rotation calls onCreate resets chrono
    // prevent screen rotation just for this fragment
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
    }
}
