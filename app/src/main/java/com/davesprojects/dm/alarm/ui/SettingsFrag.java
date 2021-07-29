package com.davesprojects.dm.alarm.ui;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.davesprojects.dm.alarm.R;
import com.davesprojects.dm.alarm.util.PermissionsHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingsFrag extends Fragment implements View.OnClickListener {
    Context con;
    View myView;

    EditText txtEditName;
    TextView txtTaskComp;
    Button btnAcceptName;
    Button btnResetTasks;
    Button btnChooseSong;
    String nameOfUser;
    SwitchCompat songSwitch;
    SwitchCompat todoSwitch;
    TextView songChoice;
    Cursor cursor;

    String choosenDat;
    String choosenSong;

    PermissionsHelper permissionsHelper;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.settings_layout, container, false);
        con = myView.getContext();


        // for back stack
        SharedPreferences.Editor prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit();
        prefEditor.putString("lastFrag", "SettingsFrag");
        prefEditor.apply();

        txtEditName = myView.findViewById(R.id.editName);
        txtTaskComp = myView.findViewById(R.id.txtTaskComp);

        btnAcceptName = myView.findViewById(R.id.btnAcceptName);
        btnAcceptName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveName();
            }
        });

        btnResetTasks = myView.findViewById(R.id.btnResetTasks);
        btnResetTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTasks();
            }
        });

        btnChooseSong = myView.findViewById(R.id.buttonSong);
        btnChooseSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseSong();
            }
        });

        songSwitch = myView.findViewById(R.id.songSwitch);
        songSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    yesSong();
                } else {
                    noSong();
                }
            }
        });

        todoSwitch = myView.findViewById(R.id.todoSwitch);
        todoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    todoSpeakOn();
                } else {
                    todoSpeakOff();
                }
            }
        });


        songChoice = myView.findViewById(R.id.textSelectedSong);


        SharedPreferences prefs = con.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        if (prefs.contains("nameOfUser")) {
            txtEditName.setText(prefs.getString("nameOfUser", ""));
        }

        if (prefs.contains("tasksCompleted")) {
            txtTaskComp.setText(String.valueOf(prefs.getInt("tasksCompleted", 0)));
        }

        if (prefs.contains("songName")) {
            songChoice.setText(prefs.getString("songName", ""));
        }

        if (prefs.contains("wakeUpToSong")) {
            if (prefs.getBoolean("wakeUpToSong", false)) {
                songSwitch.setChecked(true);
            } else {
                songSwitch.setChecked(false);
                songChoice.setText("Song: None");
            }
        }

        if (prefs.contains("todoSpeak")) {
            if (prefs.getBoolean("todoSpeak", false)) {
                todoSwitch.setChecked(true);
            } else {
                todoSwitch.setChecked(false);
            }
        }

        permissionsHelper = new PermissionsHelper(con, getActivity());
        permissionsHelper.checkPermissions();

        return myView;
    }

    public void saveName() {
        nameOfUser = txtEditName.getText().toString();

        SharedPreferences.Editor prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit();
        prefEditor.putString("nameOfUser", nameOfUser);
        prefEditor.apply();

        Toast.makeText(con, "Name saved as " + nameOfUser, Toast.LENGTH_SHORT).show();
    }

    public void resetTasks() {
        AlertDialog dialog = new AlertDialog.Builder(con)
                .setTitle("Reset Task Counter")
                .setMessage("Are you sure you would like to reset your completed tasks count to 0?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = con.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                        if (prefs.contains("tasksCompleted")) {
                            SharedPreferences.Editor prefEditor = con.getSharedPreferences("Preferences",
                                    Context.MODE_PRIVATE).edit();
                            prefEditor.putInt("tasksCompleted", 0);
                            prefEditor.apply();

                            Toast.makeText(con, "Task counter has been reset", Toast.LENGTH_SHORT).show();
                            txtTaskComp.setText("0");
                        } else {
                            // user probably hit button by mistake
                            Toast.makeText(con, "Total tasks completed already at 0", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    public void yesSong() {
        SharedPreferences.Editor prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit();
        prefEditor.putBoolean("wakeUpToSong", true);
        prefEditor.apply();

    }

    public void noSong() {
        SharedPreferences.Editor prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit();
        prefEditor.putBoolean("wakeUpToSong", false);
        prefEditor.apply();

        songChoice.setText("Song: None");
    }

    public void todoSpeakOn() {
        SharedPreferences.Editor prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit();
        prefEditor.putBoolean("todoSpeak", true);
        prefEditor.apply();
    }

    public void todoSpeakOff() {
        SharedPreferences.Editor prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit();
        prefEditor.putBoolean("todoSpeak", false);
        prefEditor.apply();
    }

    @Override
    public void onClick(View view) {

    }

    public void chooseSong() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME
        };

        cursor = myView.getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,
                null, null);

        final ArrayList<String> songs = new ArrayList<>();

        final HashMap hashSong = new HashMap<String, String>();


        // hash map idToData and songs
        while(cursor.moveToNext()) {
            songs.add(cursor.getString(2));  // needed for list view data
            // link song data to song display name

            // key = song name, value = song data (needed for shared prefs)
            hashSong.put(cursor.getString(2), cursor.getString(1));
        }

        // get songs from list view click listener -> into hash map -> get id

        // -------------------------- layout
        // dialog pop up with all songs on device
        final View dialogView = View.inflate(con, R.layout.dialog_song, null);
        final ListView lv = dialogView.findViewById(R.id.listViewSongs);


        final ArrayAdapter<String> adp = new ArrayAdapter<>(con, android.R.layout.simple_list_item_activated_1, songs);
        lv.setAdapter(adp);

        lv.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                choosenSong = String.valueOf(lv.getItemAtPosition(position));
                choosenDat = String.valueOf(hashSong.get(choosenSong));
            }
        });



        final TextView searchTV = dialogView.findViewById(R.id.textSearchSongs);

        searchTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // auto generated stub
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adp.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // more auto generated stub
            }
        });

        androidx.appcompat.app.AlertDialog dialog = new AlertDialog.Builder(myView.getContext())
                .setTitle("Choose Song")
                .setView(dialogView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor prefEditor = con.getSharedPreferences("Preferences",
                                Context.MODE_PRIVATE).edit();
                    prefEditor.putString("song", choosenDat);
                    prefEditor.putString("songName", choosenSong);
                    prefEditor.apply();

                    songChoice.setText(choosenSong);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
        }
    }
}