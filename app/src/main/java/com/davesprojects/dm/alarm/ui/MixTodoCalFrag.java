package com.davesprojects.dm.alarm.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.davesprojects.dm.alarm.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.adapter.FragmentStateAdapter;


public class MixTodoCalFrag extends Fragment implements View.OnClickListener {

    Context con;
    View myView;
    final private String[] tabTitles = new String[]{"To-Do List", "Calendar", "Quotes"};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.mix_todo_cal, container, false);
        con = myView.getContext();

        TabLayout tabLayout = myView.findViewById(R.id.tabs);

        final ViewPager2 viewPager = myView.findViewById(R.id.viewpager);
        viewPager.setAdapter(new PagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull @NotNull TabLayout.Tab tab, int position) {
                        tab.setText(tabTitles[position]);
                    }
                }
        ).attach();
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        // for back stack
        SharedPreferences.Editor prefEditor = con.getSharedPreferences("Preferences",
                Context.MODE_PRIVATE).edit();
        prefEditor.putString("lastFrag", "MixTodoCalFrag");
        prefEditor.apply();

        return myView;
    }

    public static class PagerAdapter extends FragmentStateAdapter {

        private PagerAdapter(Fragment fa) {
            super(fa);
        }

        @NotNull
        @Override
        public Fragment createFragment(int position) {
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

        @Override
        public int getItemCount() {
            return 3;
        }
    }


    @Override
    public void onClick(View view) {

    }

    // prevent screen rotation for now
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
