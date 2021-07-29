package com.davesprojects.dm.alarm.model;

import java.util.ArrayList;

/**
 * Created by dm on 1/21/17.
 */

public class Task {
    private String id;
    private String desc;

    public Task(String mId, String mDesc) {
        id = mId;
        desc = mDesc;
    }

    public String getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public static ArrayList<Task> createTaskList(ArrayList<String> ids, ArrayList<String> descs) {

        ArrayList<Task> tasks = new ArrayList<>();
        for (int i = 0; i <= ids.size() - 1; i++) {
            tasks.add(new Task(ids.get(i), descs.get(i)));
        }

        return tasks;
    }
}
