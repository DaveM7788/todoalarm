package com.davesprojects.dm.alarm.ui;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.speech.tts.TextToSpeech;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.davesprojects.dm.alarm.R;
import com.davesprojects.dm.alarm.db.DBHelper;
import com.davesprojects.dm.alarm.util.AlarmHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class WakeUpActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    Button ringtoneStop;
    Ringtone ringtone;
    int currentVolume;
    boolean wakeUpToSong = false;
    boolean speakToDoList = false;
    String wakeUpSongDat = "";
    MediaPlayer mp;

    private DBHelper dbH;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intermediate);

        if (tts == null) {
            tts = new TextToSpeech(this, this);
        }

        SharedPreferences prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        if (prefs.contains("alarmOnOff")) {
            boolean state = prefs.getBoolean("alarmOnOff", false);
            if (state) {
                SharedPreferences.Editor prefEditor = getSharedPreferences("Preferences",
                        Context.MODE_PRIVATE).edit();
                prefEditor.putBoolean("alarmOnOff", false);
                prefEditor.apply();

                playAlarm();
            }
        }

        if (prefs.contains("todoSpeak")) {
            speakToDoList = prefs.getBoolean("todoSpeak", false);
            // if speakToDoList = true, stopRingtone() will call function to speak to do list before
            // finishing
        }

        dbH = new DBHelper(this);
    }

    public void playAlarm() {
        SharedPreferences prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        if (prefs.contains("wakeUpToSong")) {
            wakeUpToSong = prefs.getBoolean("wakeUpToSong", false);
        }

        if (prefs.contains("song")) {
            wakeUpSongDat = prefs.getString("song", "");
        } else {
            wakeUpToSong = false;
        }

        if (wakeUpSongDat != null) {
            if (wakeUpSongDat.equals("")) {
                wakeUpToSong = false;
            }
        }

        ringtoneStop = findViewById(R.id.ringtoneEnd);
        ringtoneStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRingtone();
            }
        });

        if (wakeUpToSong) {
            try {
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

                mp = new MediaPlayer();
                mp.reset();
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setDataSource(wakeUpSongDat);
                mp.prepare();
                if (!mp.isPlaying()) {
                    mp.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // play an alarm sound! Wake up ; )
            // ensure ringer volume is maxed. Otherwise alarm won't make any sound if the
            // user has the phone on vibrate
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            currentVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
            int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_RING);
            audio.setStreamVolume(AudioManager.STREAM_RING, maxVolume, 0);

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            ringtone = RingtoneManager.getRingtone(this, uri);
            ringtone.play();
        }
    }

    private void setNextAlarm() {
        AlarmHandler alarmHandler = new AlarmHandler(this);
        alarmHandler.findNextAlarm();
    }

    private void stopAllSounds() {
        if (ringtone != null) {
            if (ringtone.isPlaying()) {
                // return ringtone sound to previous level before activity began
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audio.setStreamVolume(AudioManager.STREAM_RING, currentVolume, 0);
                ringtone.stop();
            }
        }

        if (mp != null) {
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);

            mp.stop();
            mp.release();
        }

        setNextAlarm();
    }

    public void stopRingtone() {
        stopAllSounds();

        if (speakToDoList) {
            String nameOfUser = "";
            SharedPreferences prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
            if (prefs.contains("nameOfUser")) {
                nameOfUser = prefs.getString("nameOfUser", "");
            }
            speakAllTasks(nameOfUser);
        }

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("goodmorning", "fromwakeup");
        startActivity(i);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.ENGLISH);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Please enable english language", Toast.LENGTH_SHORT).show();
            }
        } else {
            // notify user of the error, and keep running the app
            Toast.makeText(this, "Audio unavailable due to TextToSpeech error", Toast.LENGTH_SHORT).show();
        }
    }

    public void speakTask(String sayThis) {
        // speak out the message, api >= 21 requires extra parameter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String utteranceId=this.hashCode() + "";
            tts.speak(sayThis, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
            tts.speak(sayThis, TextToSpeech.QUEUE_FLUSH, map);
        }
    }

    public void speakAllTasks(String nameOfUser) {
        // gather info from the task database
        /* if called from onInit via AlarmReceiver, add the greeting.  This is ugly but required.
        This is done because tts doesn't wait until after it's finished speaking to speak another
        message. The first message will be skipped. There is a way around this with an utterance
        listener, but it adds a lot of ugly boilerplate.
         */
        int counter = 0;
        StringBuilder allTasks = new StringBuilder("Good Morning, " + nameOfUser + "! Your to do list for today " +
                "is as follows.");

        Cursor cursor = dbH.getAllData();

        while(cursor.moveToNext()) {
            int idx = cursor.getColumnIndex("DESCTASK");
            counter++;
            allTasks.append(" Task ").append(counter).append(". ").append(cursor.getString(idx)).append(" .");
        }

        ArrayList<String> calTasks = getCalendarTasks();
        if (calTasks.size() > 0) {
            for (String task : calTasks) {
                counter++;
                allTasks.append(" Task ").append(counter).append(". ").append(task).append(" .");
            }
        }

        cursor.close();
        dbH.close();

        speakTask(allTasks.toString());
    }

    public ArrayList<String> getCalendarTasks() {
        final String[] INSTANCE_PROJECTION = new String[] {
                CalendarContract.Instances.EVENT_ID,      // 0
                CalendarContract.Instances.BEGIN,         // 1
                CalendarContract.Instances.TITLE,          // 2
                CalendarContract.Instances.DTSTART,
                CalendarContract.Instances.START_MINUTE
        };

        // other indexes not used
        final int PROJECTION_TITLE_INDEX = 2;
        final int PROJECTION_STARTMINUTE_INDEX = 4;

        Calendar beginTime = Calendar.getInstance();
        int date = beginTime.get(Calendar.DAY_OF_MONTH);
        int year = beginTime.get(Calendar.YEAR);
        int month = beginTime.get(Calendar.MONTH);
        beginTime.set(year, month, date);
        Calendar endTime = Calendar.getInstance();
        endTime.set(year, month, date + 1);
        long startMillis = beginTime.getTimeInMillis();
        long endMillis = endTime.getTimeInMillis();

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        Cursor cur;
        ContentResolver cr = getContentResolver();
        cur =  cr.query(builder.build(),
                INSTANCE_PROJECTION,
                null,
                null,
                null);

        String title;
        String startmin;

        ArrayList<String> calTasksForToday = new ArrayList<>();

        if (cur != null) {
            while (cur.moveToNext()) {
                // Get the field values
                title = cur.getString(PROJECTION_TITLE_INDEX);
                startmin = cur.getString(PROJECTION_STARTMINUTE_INDEX);
                calTasksForToday.add(convertMinsSinceMidnightToTime(startmin) + " - " + title);
            }
            cur.close();
        }

        return calTasksForToday;
    }

    public String convertMinsSinceMidnightToTime(String startmin) {
        int startminCalc = Integer.parseInt(startmin);
        int hour = startminCalc / 60;
        int minutes = startminCalc % 60;
        String amOrPM = "AM";
        String minutesStr = String.valueOf(minutes);

        if (hour >= 12) {
            amOrPM = "PM";
            if (hour > 12) {
                hour -= 12;  // prevents 8:30 PM being shown as 20:30 PM
            }
        }

        if (minutes < 10) {
            minutesStr = "0" + minutesStr;
        }

        return (hour) + ":" + minutesStr + " " + amOrPM;
    }

    // in case user accidentally hits the home button instead of ok, STOP the ALARM! ; )
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAllSounds();

        // tts can consume a lot of resources if left on
        tts.stop();
        tts.shutdown();

        if (dbH != null) dbH.close();
    }


    // prevents screen rotation from disabling alarm sound
    @Override
    public void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onPause() {
        super.onPause();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }
}
