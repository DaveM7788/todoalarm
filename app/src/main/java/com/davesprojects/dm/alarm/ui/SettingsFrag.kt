package com.davesprojects.dm.alarm.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.davesprojects.dm.alarm.R
import com.davesprojects.dm.alarm.util.PermissionsHelper
import java.util.*

class SettingsFrag : Fragment(), View.OnClickListener {

    private lateinit var con: Context
    private lateinit var myView: View
    private lateinit var txtEditName: EditText
    private lateinit var txtTaskComp: TextView
    private lateinit var btnAcceptName: Button
    private lateinit var btnResetTasks: Button
    private lateinit var btnChooseSong: Button
    private lateinit var btnCalOptions: Button
    var nameOfUser: String? = null
    private lateinit var songSwitch: SwitchCompat
    private lateinit var todoSwitch: SwitchCompat
    private lateinit var songChoice: TextView
    var cursor: Cursor? = null
    var choosenDat: String? = null
    var choosenSong: String? = null
    var permissionsHelper: PermissionsHelper? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        myView = inflater.inflate(R.layout.settings_layout, container, false)
        con = myView.context


        // for back stack
        val prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit()
        prefEditor.putString("lastFrag", "SettingsFrag")
        prefEditor.apply()
        txtEditName = myView.findViewById(R.id.editName)
        txtTaskComp = myView.findViewById(R.id.txtTaskComp)
        btnAcceptName = myView.findViewById(R.id.btnAcceptName)
        btnAcceptName.setOnClickListener(View.OnClickListener { saveName() })
        btnResetTasks = myView.findViewById(R.id.btnResetTasks)
        btnResetTasks.setOnClickListener(View.OnClickListener { resetTasks() })
        btnChooseSong = myView.findViewById(R.id.buttonSong)
        btnChooseSong.setOnClickListener(View.OnClickListener { chooseSong() })
        btnCalOptions = myView.findViewById(R.id.btnChangeCalOpts)
        btnCalOptions.setOnClickListener(View.OnClickListener { goCalOptions() })

        songSwitch = myView.findViewById(R.id.songSwitch)
        songSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, b ->
            if (b) {
                yesSong()
            } else {
                noSong()
            }
        })
        todoSwitch = myView.findViewById(R.id.todoSwitch)
        todoSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, b ->
            if (b) {
                todoSpeakOn()
            } else {
                todoSpeakOff()
            }
        })
        songChoice = myView.findViewById(R.id.textSelectedSong)
        val prefs = con.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        if (prefs.contains("nameOfUser")) {
            txtEditName.setText(prefs.getString("nameOfUser", ""))
        }
        if (prefs.contains("tasksCompleted")) {
            txtTaskComp.text = prefs.getInt("tasksCompleted", 0).toString()
        }
        if (prefs.contains("songName")) {
            songChoice.text = prefs.getString("songName", "")
        }
        if (prefs.contains("wakeUpToSong")) {
            if (prefs.getBoolean("wakeUpToSong", false)) {
                songSwitch.isChecked = true
            } else {
                songSwitch.isChecked = false
                songChoice.text = getString(R.string.song_none)
            }
        }
        if (prefs.contains("todoSpeak")) {
            todoSwitch.isChecked = prefs.getBoolean("todoSpeak", false)
        }
        permissionsHelper = PermissionsHelper(con, activity)
        permissionsHelper?.checkPermissions()
        return myView
    }

    private fun goCalOptions() {
        val fragmentManager: FragmentManager? = activity?.supportFragmentManager
        fragmentManager?.beginTransaction()
                ?.addToBackStack("")
                ?.replace(R.id.content_frame, CalendarOptionsFrag())
                ?.commit()
    }

    private fun saveName() {
        nameOfUser = txtEditName.text.toString()
        val prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit()
        prefEditor.putString("nameOfUser", nameOfUser)
        prefEditor.apply()
        Toast.makeText(con, "Name saved as $nameOfUser", Toast.LENGTH_SHORT).show()
    }

    private fun resetTasks() {
        val dialog = AlertDialog.Builder(con)
                .setTitle("Reset Task Counter")
                .setMessage("Are you sure you would like to reset your completed tasks count to 0?")
                .setPositiveButton("Yes") { _, _ ->
                    val prefs = con.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
                    if (prefs.contains("tasksCompleted")) {
                        val prefEditor = con.getSharedPreferences("Preferences",
                                Context.MODE_PRIVATE).edit()
                        prefEditor.putInt("tasksCompleted", 0)
                        prefEditor.apply()
                        Toast.makeText(con, "Task counter has been reset", Toast.LENGTH_SHORT).show()
                        txtTaskComp.text = "0"
                    } else {
                        // user probably hit button by mistake
                        Toast.makeText(con, "Total tasks completed already at 0", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
        dialog.show()
    }

    private fun yesSong() {
        val prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit()
        prefEditor.putBoolean("wakeUpToSong", true)
        prefEditor.apply()
    }

    private fun noSong() {
        val prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit()
        prefEditor.putBoolean("wakeUpToSong", false)
        prefEditor.apply()
        songChoice.text = getString(R.string.song_none)
    }

    private fun todoSpeakOn() {
        val prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit()
        prefEditor.putBoolean("todoSpeak", true)
        prefEditor.apply()
    }

    private fun todoSpeakOff() {
        val prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit()
        prefEditor.putBoolean("todoSpeak", false)
        prefEditor.apply()
    }

    private fun getReadAudioPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (!hasAudioPermissions()) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 9991
                )
            }
        }
    }

    private fun hasAudioPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_DENIED
        } else {
            true
        }
    }

    override fun onClick(view: View) {}

    private fun chooseSong() {
        if (!hasAudioPermissions()) {
            getReadAudioPermissions()
            Toast.makeText(
                requireContext(), getString(R.string.please_allow_audio), Toast.LENGTH_LONG
            ).show()
            return
        }

        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME
        )
        cursor = myView.context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,
                null, null)
        val songs = ArrayList<String?>()
        val hashSong: HashMap<String?, String?> = HashMap()


        // hash map idToData and songs
        while (cursor?.moveToNext() == true) {
            songs.add(cursor?.getString(2)) // needed for list view data
            // link song data to song display name

            // key = song name, value = song data (needed for shared prefs)
            hashSong[cursor?.getString(2)] = cursor?.getString(1)
        }

        // get songs from list view click listener -> into hash map -> get id

        // -------------------------- layout
        // dialog pop up with all songs on device
        val dialogView = View.inflate(con, R.layout.dialog_song, null)
        val lv = dialogView.findViewById<ListView>(R.id.listViewSongs)
        val adp = ArrayAdapter(con, android.R.layout.simple_list_item_activated_1, songs)
        lv.adapter = adp
        lv.choiceMode = AbsListView.CHOICE_MODE_SINGLE
        lv.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            choosenSong = lv.getItemAtPosition(position).toString()
            choosenDat = hashSong[choosenSong].toString()
        }
        val searchTV = dialogView.findViewById<TextView>(R.id.textSearchSongs)
        searchTV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // auto generated stub
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                adp.filter.filter(charSequence)
            }

            override fun afterTextChanged(editable: Editable) {
                // more auto generated stub
            }
        })
        val dialog = AlertDialog.Builder(myView.context)
                .setTitle("Choose Song")
                .setView(dialogView)
                .setPositiveButton("Ok") { _, _ ->
                    val prefEditor = con.getSharedPreferences("Preferences",
                            Context.MODE_PRIVATE).edit()
                    prefEditor.putString("song", choosenDat)
                    prefEditor.putString("songName", choosenSong)
                    prefEditor.apply()
                    songChoice.text = choosenSong
                }
                .setNegativeButton("Cancel", null)
                .create()
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cursor != null) {
            cursor?.close()
        }
    }
}