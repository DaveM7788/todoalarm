package com.davesprojects.dm.alarm.ui;

import static com.davesprojects.dm.alarm.util.UsefulConstKt.STOPWATCH_FRAG;
import static com.davesprojects.dm.alarm.util.UsefulConstKt.TIMER_FRAG;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.AlarmManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.davesprojects.dm.alarm.util.PermissionsHelper;
import com.davesprojects.dm.alarm.R;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    NavigationView navigationView;
    PermissionsHelper permissionsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String goodmorning = bundle.getString("goodmorning");
            if (goodmorning != null) {
                if (goodmorning.equals("fromwakeup")) {
                    Fragment fragment = new MixTodoCalFrag();
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("").commit();
                    bundle.clear();
                } else {
                    defaultFrag();
                }
            }
            else {
                defaultFrag();
            }
        } else {
            defaultFrag();
        }

        permissionsHelper = new PermissionsHelper(getApplicationContext(), this);
        permissionsHelper.checkPermissions();

        getAlarmPermissions();
    }

    public void defaultFrag() {
        Fragment fragment = new AlarmFrag();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("").commit();
        navigationView.getMenu().getItem(0).setChecked(true);
    }


    // prevent fragment from changing whenever screen rotates
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        SharedPreferences prefsF = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        if (prefsF.contains("lastFrag")) {
            String lastFrag = prefsF.getString("lastFrag", "");
            outState.putString("lastFragState", lastFrag);
        }
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String lastFrag = savedInstanceState.getString("lastFragState");
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (lastFrag.equals("AlarmFrag")) {
            fragmentManager.beginTransaction()
                    .addToBackStack("")
                    .replace(R.id.content_frame, new AlarmFrag())
                    .commit();
        } else if (lastFrag.equals("TodoFrag")) {
            fragmentManager.beginTransaction()
                    .addToBackStack("")
                    .replace(R.id.content_frame, new MixTodoCalFrag())
                    .commit();
        } else if (lastFrag.equals("SettingsFrag")) {
            fragmentManager.beginTransaction()
                    .addToBackStack("")
                    .replace(R.id.content_frame, new SettingsFrag())
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .addToBackStack("")
                    .replace(R.id.content_frame, new AlarmFrag())
                    .commit();
        }
    }

    // ensure that the data stored in prefs appears when restarting the activity
    // also check to see if an alarm's pending intent is currently active
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

        // handle back clicking correct navigation drawer highlights
        navigationView.getMenu().getItem(0).setChecked(false);
        navigationView.getMenu().getItem(1).setChecked(false);
        navigationView.getMenu().getItem(2).setChecked(false);
        navigationView.getMenu().getItem(3).setChecked(false);

        SharedPreferences prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        if (prefs.contains("lastFrag")) {
            String lastFrag = prefs.getString("lastFrag", "");
            if (lastFrag.equals("AlarmFrag")) {
                navigationView.getMenu().getItem(0).setChecked(true);
            } else if (lastFrag.equals("MixTodoCalFrag")) {
                navigationView.getMenu().getItem(1).setChecked(true);
            } else if (lastFrag.equals("StopwatchFrag")) {
                navigationView.getMenu().getItem(2).setChecked(true);
            } else if (lastFrag.equals("SettingsFrag")) {
                navigationView.getMenu().getItem(3).setChecked(true);
            }
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

     */

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_about)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // dismiss dialog
                        }
                    });
            builder.create();
            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

     */

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_alarm) {
            fragmentManager.beginTransaction()
                    .addToBackStack("")
                    .replace(R.id.content_frame, new AlarmFrag())
                    .commit();
        } else if (id == R.id.nav_todolist) {
            fragmentManager.beginTransaction()
                    .addToBackStack("")
                    .replace(R.id.content_frame, new MixTodoCalFrag())
                    .commit();
        } else if (id == R.id.nav_settings) {
            fragmentManager.beginTransaction()
                    .addToBackStack("")
                    .replace(R.id.content_frame, new SettingsFrag())
                    .commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean hasAlarmPermissions() {
        if (Build.VERSION.SDK_INT >= 31) {
            AlarmManager alarmManager =
                    (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            return alarmManager.canScheduleExactAlarms();
        } else {
            return true;
        }
    }

    private void getAlarmPermissions() {
        if (Build.VERSION.SDK_INT >= 31) {
            if (!hasAlarmPermissions()) {
                Toast.makeText(
                        this, getString(R.string.please_allow_alarm), Toast.LENGTH_LONG
                ).show();

                startActivity(
                        new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:com.davesprojects.dm.alarm"))
                );
            }
        }
    }
}
