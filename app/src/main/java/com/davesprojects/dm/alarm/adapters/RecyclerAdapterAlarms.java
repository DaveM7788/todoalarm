package com.davesprojects.dm.alarm.adapters;



import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.davesprojects.dm.alarm.model.AlarmDP;
import com.davesprojects.dm.alarm.util.AlarmHandler;
import com.davesprojects.dm.alarm.util.AlarmUpdate;
import com.davesprojects.dm.alarm.R;
import com.davesprojects.dm.alarm.db.DBHelper;

import java.util.ArrayList;

public class RecyclerAdapterAlarms extends RecyclerView.Adapter<RecyclerAdapterAlarms.ViewHolder> {

    private ArrayList<AlarmDP> mAlarms;
    private Context mContext;
    private DBHelper dbH;
    private TextView alarmTimeTV;
    private AlarmHandler alarmHandler;


    public RecyclerAdapterAlarms(Context context, ArrayList<AlarmDP> adapterAlarms, TextView tv) {
        mAlarms = adapterAlarms;
        mContext = context;
        alarmTimeTV = tv;
        dbH = new DBHelper(mContext);
        alarmHandler = new AlarmHandler(mContext);
    }

    private Context getContext() {
        return mContext;
    }

    private void updateAlarmTime() {
        String t = alarmHandler.findNextAlarm();
        alarmTimeTV.setText(t);
    }

    private void toggleAlarmOn(int pos) {
        AlarmDP alarm = mAlarms.get(pos);
        String days = alarm.getDays();
        String id = alarm.getId();
        String time = alarm.getTime();

        dbH.changeAlarmState(alarm.getId(), "on");

        mAlarms.set(pos, new AlarmDP(id, time, days, "on"));

        // may alter the next alarm to go off
        updateAlarmTime();

        dbH.close();
    }

    private void toggleAlarmOff(int pos) {
        AlarmDP alarm = mAlarms.get(pos);
        String days = alarm.getDays();
        String id = alarm.getId();
        String time = alarm.getTime();

        dbH.changeAlarmState(alarm.getId(), "off");

        mAlarms.set(pos, new AlarmDP(id, time, days, "off"));

        // may alter the next alarm to go off
        updateAlarmTime();

        dbH.close();
    }

    // modification of the recyclerAdapter cards and corresponding db entries
    private void removeAt(int i) {
        final int pos = i;
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle("Delete Alarm")
                .setMessage("Are you sure you would like to delete this alarm?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlarmDP alarm = mAlarms.get(pos);
                        dbH.deleteAlarmData(alarm.getId());
                        mAlarms.remove(pos);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, mAlarms.size());

                        // may later the next alarm to go off
                        updateAlarmTime();

                        dbH.close();
                    }
                })
                .setNegativeButton("No", null)
                .create();
        dialog.show();
    }

    private void updateAt(int i) {
        AlarmDP alarm = mAlarms.get(i);
        AlarmUpdate alarmUpdate = new AlarmUpdate(mContext, mAlarms, this, i, alarm.getId(), alarmTimeTV);
        alarmUpdate.updateAlarm(alarm.getId());
    }


    // boilerplate for extended classes
    // -----------------------------------------------------------------------------------------------------------------------------------------
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_card_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder vH, int i) {
        AlarmDP alarm = mAlarms.get(i);
        vH.itemTitle.setText(alarm.getTime());
        vH.itemDetail.setText(alarm.getDays());
        vH.itemId.setText(alarm.getId());

        if (alarm.getOnoff().equals("on")) {
            vH.onOff.setChecked(true);
        } else {
            vH.onOff.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return mAlarms.size();
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // inner class to hold a view for each card inside the RecyclerAdapterToDo
    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView itemTitle;
        private TextView itemDetail;
        private TextView itemId;
        private Button btnDelete;
        private Switch onOff;

        private ViewHolder(final View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemDetail = itemView.findViewById(R.id.item_detail);
            itemId = itemView.findViewById(R.id.item_id);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            onOff = itemView.findViewById(R.id.switch_onoff);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeAt(getAdapterPosition());
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateAt(getAdapterPosition());
                }
            });

            onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        toggleAlarmOn(getAdapterPosition());
                    } else {
                        toggleAlarmOff(getAdapterPosition());
                    }
                }
            });
        }
    }
    // end inner class ------------------------------------------------------------------------------------------------------------------------------------
}
