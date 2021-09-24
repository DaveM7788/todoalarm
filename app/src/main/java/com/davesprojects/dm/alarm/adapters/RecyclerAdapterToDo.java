package com.davesprojects.dm.alarm.adapters;



import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import com.davesprojects.dm.alarm.R;
import com.davesprojects.dm.alarm.model.Task;
import com.davesprojects.dm.alarm.db.DBHelper;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RecyclerAdapterToDo extends RecyclerView.Adapter<RecyclerAdapterToDo.ViewHolder> {

    private ArrayList<Task> mTasks;
    private Context mContext;
    private DBHelper dbH;
    boolean checkBoxFlagEdit = false;
    int daysRepeatEdit = 0;
    int tasksCompleted;

    public RecyclerAdapterToDo(Context context, ArrayList<Task> adapterTasks) {
        mTasks = adapterTasks;
        mContext = context;
        dbH = new DBHelper(mContext);
    }

    private Context getContext() {
        return mContext;
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // inner class to hold a view for each card inside the RecyclerAdapterToDo
    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView itemTitle;
        public TextView itemDetail;
        public TextView itemId;
        public Button btnDelete;

        public ViewHolder(final View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemDetail = itemView.findViewById(R.id.item_detail);
            itemId = itemView.findViewById(R.id.item_id);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeAt(getAdapterPosition());
                    updateTaskCounter();
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editAt(getAdapterPosition());
                }
            });
        }

        public void updateTaskCounter() {
            SharedPreferences prefs = getContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
            if (prefs.contains("tasksCompleted")) {
                tasksCompleted = prefs.getInt("tasksCompleted", 0);
                tasksCompleted++;
            } else {
                // user has completed first task
                tasksCompleted = 1;
            }
            SharedPreferences.Editor prefEditor = getContext().getSharedPreferences("Preferences",
                    Context.MODE_PRIVATE).edit();
            prefEditor.putInt("tasksCompleted", tasksCompleted);
            prefEditor.apply();

            Snackbar.make(itemView, "Total Tasks Completed: " + String.valueOf(tasksCompleted),
                    Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
    }
    // end inner class ------------------------------------------------------------------------------------------------------------------------------------

    // modification of the recyclerAdapter cards and corresponding db entries
    private void removeAt(int i) {
        Task task = mTasks.get(i);
        dbH.deleteData(task.getId());
        dbH.close();
        mTasks.remove(i);
        notifyItemRemoved(i);
        notifyItemRangeChanged(i, mTasks.size());
    }

    private void editAt(int i) {
        final int position = i;
        final Task task = mTasks.get(i);

        View checkBoxView = View.inflate(getContext(), R.layout.edit, null);

        final EditText taskEditText = (EditText) checkBoxView.findViewById(R.id.editSomeTask);
        taskEditText.setText(task.getDesc());
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Edit Task")
                .setMessage("Please edit the task info")
                .setView(checkBoxView)
                .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strTask = String.valueOf(taskEditText.getText());

                        dbH.updateData(task.getId(), strTask);
                        dbH.close();

                        mTasks.set(position, new Task(task.getId(), strTask));
                        notifyItemChanged(position);

                        // reset days for repeating tasks in case user adds another task
                        daysRepeatEdit = 0;

                        // also reset the checkbox flag in case user doesn't click on check box again
                        checkBoxFlagEdit = false;
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    // -----------------------------------------------------------------------------------------------------------------------------------------
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.todo_card, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder vH, int i) {
        Task task = mTasks.get(i);
        vH.itemTitle.setText(task.getDesc());
        vH.itemId.setText(task.getId());
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }
}
