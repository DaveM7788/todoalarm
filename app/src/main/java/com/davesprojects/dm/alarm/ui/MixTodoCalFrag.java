package com.davesprojects.dm.alarm.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.davesprojects.dm.alarm.R;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MixTodoCalFrag extends Fragment implements View.OnClickListener {
    Context con;
    View myView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.mix_todo_cal, container, false);
        con = myView.getContext();

        TabLayout tabLayout = myView.findViewById(R.id.tabs);

        final ViewPager viewPager = myView.findViewById(R.id.viewpager);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        viewPager.setAdapter(new PagerAdapter(fm));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // for back stack
        SharedPreferences.Editor prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit();
        prefEditor.putString("lastFrag", "MixTodoCalFrag");
        prefEditor.apply();

        return myView;
    }


    public static class PagerAdapter extends FragmentStatePagerAdapter {

        private PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        private String[] tabTitles = new String[]{"To-Do List", "Calendar", "Quotes"};

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ToDoFragmentTab();
                case 1:
                    return new CalendarFragmentTab();
                case 2:
                    return new QuoteFragmentTab();
                default:
                    return null;
            }
        }
    }

    @Override
    public void onClick(View view) {

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
