package com.davesprojects.dm.alarm.ui;

import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import androidx.annotation.Nullable;

import com.davesprojects.dm.alarm.R;
import com.davesprojects.dm.alarm.model.TodayCalEvents;
import com.davesprojects.dm.alarm.adapters.RecyclerAdapterToDo;
import com.davesprojects.dm.alarm.db.DBHelper;
import com.davesprojects.dm.alarm.model.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;

public class ToDoFragmentTab extends Fragment implements View.OnClickListener {
    Context con;
    View myView;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;
    ArrayList<Task> tasks;
    private DBHelper dbH;
    int dbAutoId;
    int daysRepeat = 0;
    boolean checkBoxFlag = false;
    FloatingActionButton fab;
    ArrayList<String> taskList = new ArrayList<>();
    ArrayList<String> taskId = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.todo_list_w_fab, container, false);
        con = myView.getContext();

        // for back stack
        SharedPreferences.Editor prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit();
        prefEditor.putString("lastFrag", "TodoFrag");
        prefEditor.apply();

        dbH = new DBHelper(con);
        // collect data from sqlite database to create list of tasks when user first opens screen
        // As seen below, there is 1 ArrayList per column in the database
        Cursor cursor = dbH.getAllData();
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex("DESCTASK");
            int id = cursor.getColumnIndex("ID");

            taskList.add(cursor.getString(idx));
            taskId.add(cursor.getString(id));
        }
        cursor.close();
        dbH.close();
        // each task is an object that requires an id (that matches the database id), a description,
        tasks = Task.createTaskList(taskId, taskList);

        // create the view for the to-do list activity, a recycler view is similar to a list view
        // but has much more advanced features
        recyclerView = myView.findViewById(R.id.recycler_view);
        // the recyclerView needs to hold task objects
        adapter = new RecyclerAdapterToDo(con, tasks);
        // all recycler views require a layout manager, linear is the simplest
        layoutManager = new LinearLayoutManager(con);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        fab = myView.findViewById(R.id.fAB);
        fab.setOnClickListener(this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 || dy < 0 && fab.isShown()) {
                    fab.hide();
                }
            }

            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    // wait for 1.1 seconds
                    final Handler aniHandler = new Handler();
                    aniHandler.postDelayed(new Runnable(){
                        public void run(){
                            fab.show();
                        }
                    }, 1100);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        importTasksFromCal();

        return myView;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fAB) {
            addTask();
        }
    }

    public void insertTaskDB(String task) {
        dbH.insertData(task);
        dbH.close();

        // get id of last added item, sloppy but required to correctly give
        // the proper id to the ArrayList of task objects
        Cursor cursor = dbH.getAllData();
        while (cursor.moveToNext()) {
            dbAutoId = cursor.getColumnIndex("ID");
            dbAutoId = Integer.parseInt(cursor.getString(dbAutoId));
        }
        cursor.close();
        dbH.close();

        tasks.add(new Task(String.valueOf(dbAutoId) , task));
        adapter.notifyItemInserted(adapter.getItemCount() - 1);

        // reset days for repeating tasks in case user adds another task
        daysRepeat = 0;
    }

    public void addTask() {
        View checkBoxView = View.inflate(con, R.layout.checkbox, null);
        final CheckBox checkBox = checkBoxView.findViewById(R.id.checkboxCal);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBoxFlag = isChecked;
            }
        });

        // task description info
        final EditText taskEditText = checkBoxView.findViewById(R.id.someTask);
        AlertDialog dialog = new AlertDialog.Builder(con)
                .setTitle("Add a New Task")
                .setMessage("Please enter the task info")
                .setView(checkBoxView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String task = String.valueOf(taskEditText.getText());
                        if (!task.isEmpty()) {
                            if (checkBoxFlag) {
                                Intent intent = new Intent(Intent.ACTION_INSERT)
                                        .setData(CalendarContract.Events.CONTENT_URI)
                                        .putExtra(CalendarContract.Events.TITLE, task);
                                startActivityForResult(intent, 198);
                            } else {
                                insertTaskDB(task);
                            }
                        }
                        // also reset the checkbox flag in case user doesn't click on check box again
                        checkBoxFlag = false;
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 198) {
            // refresh the fragment otherwise calendar won't update with new task
            // until fragment is manually re-opened
            try {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .addToBackStack("")
                        .replace(R.id.content_frame, new MixTodoCalFrag())
                        .commit();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    // options menu stuff
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // MenuItem menuItem = menu.findItem(R.id.menu_item_to_change_icon_for);
        // You can change the state of the menu item here if you call getActivity().supportInvalidateOptionsMenu(); somewhere in your code
    }


    // add tasks to to-do list from calendar for just one day
    public void importTasksFromCal() {
        Calendar beginTime = Calendar.getInstance();
        int date = beginTime.get(Calendar.DAY_OF_MONTH);
        int year = beginTime.get(Calendar.YEAR);
        int month = beginTime.get(Calendar.MONTH);
        String result = year + "," + month + "," + date;

        refreshTaskCalendarDB(result);


        // check if already done for today
        SharedPreferences.Editor prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit();

        Cursor cursor = dbH.getAllCalendarTodayTasks();
        ArrayList<String> alreadyAdded = new ArrayList<>();
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex("DESCTASK");

            alreadyAdded.add(cursor.getString(idx));
        }
        cursor.close();
        dbH.close();

        ArrayList<String> calTasksForToday = new TodayCalEvents(con).getTodayCalEvents(true);
        if (calTasksForToday != null) {
            if (calTasksForToday.size() > 0) {
                for (String task : calTasksForToday) {
                    if (!alreadyAdded.contains(task)) {
                        insertTaskDB(task);
                        insertTaskCalendarDB(task);
                    }
                }
            }
        }

        prefEditor.putString("lastDateAdded", year + "," + month + "," + date);
        prefEditor.apply();
    }

    public void refreshTaskCalendarDB(String check) {
        SharedPreferences prefs = con.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        if (prefs.contains("lastDateAdded")) {
            if (!check.equals(prefs.getString("lastDateAdded", ""))) {
                dbH.refreshCalendarTasksDB();
                dbH.close();
            }
        }
    }

    public void insertTaskCalendarDB(String task) {
        dbH.insertCalendarTodayTasks(task);
        dbH.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbH != null) {
            dbH.close();
        }
    }
}
