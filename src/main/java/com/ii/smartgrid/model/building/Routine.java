package com.ii.smartgrid.model.building;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.ii.smartgrid.utils.TimeUtils;

public class Routine {

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
            // Check if tasks have to be divided and if are all valid
            return false;
        }

        // Check if one of the new tasks has conflict with an old one
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

                    if ((oldStart.isBefore(newEnd)) && (newStart.isBefore(oldEnd))) {
                        // Conflict
                        return false;
                    }
                }
            }
        }
        // All the newTasks can be added
        this.tasks.addAll(newTasks);
        return true;
    }

    private TaskStatus checkTask(Task task) {
        LocalTime start = TimeUtils.getLocalTimeFromString(task.getStartTime());
        LocalTime end = TimeUtils.getLocalTimeFromString(task.getEndTime());
        if (start.isAfter(end)) {
            // Task is running during midnight -> has to be split into two sub tasks
            return TaskStatus.TO_SPLIT;
        } else if (start.equals(end)) {
            // Useless task
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
                // The task has to be split into two sub tasks
                Task task1 = new Task(curTask.getApplianceName(), curTask.getStartTime(), "00:00");
                Task task2 = new Task(curTask.getApplianceName(), "00:00", curTask.getEndTime());
                tasksIterator.add(task1);
                tasksIterator.add(task2);
            } else if (curTaskStatus == TaskStatus.TO_DELETE) {
                // If a task is invalid, the operation fails
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

        // Check if all the newTasks are in the routine
        for (Task task : newTasks) {
            if (!this.tasks.contains(task)) {
                return false;
            }
        }

        // All the newTasks can be removed
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
