package com.ii.smartgrid.smartgrid.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.ii.smartgrid.smartgrid.utils.TimeUtils;

public class Routine {

    /*
     * "washing_machine1": ["12:00", "12:30"]
     *
     * */

    private List<Task> tasks;

    public Routine() {
        super();
        tasks = new ArrayList<>();
    }

    public Routine(List<Task> tasks) {
        this.tasks = tasks;
    }

    public boolean addTasks(List<Task> newTasks, List<Appliance> appliances) {

        if (!divideTasks(newTasks)) {
            return false;
        }

        // check if one of the new tasks has conflict with already running appliance
        for (Task newTask : newTasks) {
            String newApplianceName = newTask.getApplianceName();
            Appliance newAppliance = appliances.stream().filter(appliance -> appliance.getName().equals(newApplianceName)).findFirst().get();

            LocalTime newStart = TimeUtils.getLocalTimeFromString(newTask.getStartTime());
            LocalTime newEnd = TimeUtils.getLocalTimeFromString(newTask.getEndTime());
            for (Task oldTask : this.tasks) {
                String oldApplianceName = oldTask.getApplianceName();
                Appliance oldAppliance = appliances.stream().filter(appliance -> appliance.getName().equals(oldApplianceName)).findFirst().get();
                if (oldAppliance.equals(newAppliance)) {
                    LocalTime oldStart = TimeUtils.getLocalTimeFromString(oldTask.getStartTime());
                    LocalTime oldEnd = TimeUtils.getLocalTimeFromString(oldTask.getEndTime());

                    //conflict caused by intersection of intervals

                    // start old < end new && start new < end old

                    // start old 12.30; end old 13.30
                    // start new 11; end new 14; intervallo compreso completamente funziona

                    // start old 11.30; end old 13.30
                    // start new 12; end new 13; intervallo compreso completamente al contrario funziona

                    // start old 12.30; end old 13.30
                    // start new 11; end new 13; intervallo compreso a sinistra funziona

                    // start old 12.30; end old 13.30
                    // start new 13; end new 14; intervallo compreso compreso a destra funziona

                    // start old 12.30; end old 13.30
                    // start new 13.30; end new 16; caso giusto funziona
                    if ((oldStart.isBefore(newEnd)) && (newStart.isBefore(oldEnd))) {
                        return false;
                    }
                }
            }
        }
        //all the newTasks can be added
        this.tasks.addAll(newTasks);
        return true;
    }

    private TaskStatus checkTask(Task task) {
        LocalTime start = TimeUtils.getLocalTimeFromString(task.getStartTime());
        LocalTime end = TimeUtils.getLocalTimeFromString(task.getEndTime());
        if (start.isAfter(end)) {
            return TaskStatus.TO_SPLIT;
        } else if (start.equals(end)) {
            return TaskStatus.TO_DELETE;
        }
        return TaskStatus.OK;
    }

    private boolean divideTasks(List<Task> tasks) {
        ListIterator<Task> tasksIterator = tasks.listIterator();
        while (tasksIterator.hasNext()) {
            Task curTask = tasksIterator.next();
            TaskStatus curTaskStatus = checkTask(curTask);
            if (curTaskStatus == TaskStatus.TO_SPLIT) {
                Task task1 = new Task(curTask.getApplianceName(), curTask.getStartTime(), "00:00");
                Task task2 = new Task(curTask.getApplianceName(), "00:00", curTask.getEndTime());
                tasksIterator.add(task1);
                tasksIterator.add(task2);
            } else if (curTaskStatus == TaskStatus.TO_DELETE) {
                return false;
            }
        }
        return true;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public boolean removeTasks(List<Task> newTasks) {

        if (!divideTasks(newTasks)) {
            return false;
        }

        //check if all the newTasks are in the routine
        for (Task task : newTasks) {
            if (!this.tasks.contains(task)) {
                return false;
            }
        }

        //all the newTasks can be removed
        for (Task task : newTasks) {
            this.tasks.remove(task);
        }

        return true;
    }

    @Override
    public String toString() {
        return "Routine [tasks=" + tasks + "]";
    }

    private enum TaskStatus {OK, TO_SPLIT, TO_DELETE}

}
