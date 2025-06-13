package com.ii.smartgrid.smartgrid.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class Routine {
	
	/*
	 * "washing_machine1": ["12:00", "12:30"]
	 *
	 * */

	private List<Task> tasks;
	
	public Routine() {
		super();
		tasks = new ArrayList<Task>();
	}
	
	public Routine(List<Task> tasks) {
		//this.tasks = new ArrayList<Task>();
		this.tasks = tasks;
	}
	
	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	@Override
	public String toString() {
		return "Routine [tasks=" + tasks + "]";
	}

	
	
	public boolean addTasks(List<Task> newTasks) {
		
		if(divideTasks(newTasks) == false) {
			return false;
		}
		
		// check if one of the new tasks has conflict with already running appliance
		for(Task new_task : newTasks){
			Appliance appliance = new_task.getAppliance();
			LocalTime new_start = LocalTime.parse(new_task.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
			LocalTime new_end = LocalTime.parse(new_task.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
			
			for(Task old_task : this.tasks){
				if (old_task.getAppliance().equals(appliance)){
					LocalTime old_start = LocalTime.parse(old_task.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
					LocalTime old_end = LocalTime.parse(old_task.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
					
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
					if((old_start.compareTo(new_end) < 0) && (new_start.compareTo(old_end) < 0)){
						return false;
					}
				}
			}
		}
		//all the newTasks can be added
		for(Task task : newTasks){
			this.tasks.add(task);
		}
		return true;
	}


	private enum TaskStatus {OK, TO_SPLIT, TO_DELETE};
	
	private TaskStatus checkTask(Task task) {
		LocalTime start = LocalTime.parse(task.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
		LocalTime end = LocalTime.parse(task.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
		if(start.compareTo(end) > 0) {
			return TaskStatus.TO_SPLIT;
		} else if(start.compareTo(end) == 0) {
			return TaskStatus.TO_DELETE;
		}
		return TaskStatus.OK;
	}
	
	private boolean divideTasks(List<Task> tasks) {
		ListIterator<Task> listIterator = tasks.listIterator();
		while (listIterator.hasNext()) {
			Task curTask = listIterator.next();
			TaskStatus curTaskStatus = checkTask(curTask);
			if(curTaskStatus == TaskStatus.TO_SPLIT) {
				Task task1 = new Task(curTask.getAppliance(), curTask.getStartTime(), "00:00");
				Task task2 = new Task(curTask.getAppliance(), "00:00", curTask.getEndTime());
				listIterator.add(task1);
				listIterator.add(task2);
			}
			else if(curTaskStatus == TaskStatus.TO_DELETE) {
				return false;
			}
		}
		return true;
	}
	
	public boolean removeTasks(List<Task> newTasks) {
		
		if(divideTasks(newTasks) == false) {
			return false;
		}
		
		//check if all the newTasks are in the routine
		for(Task task : newTasks){
			if(!this.tasks.contains(task)) {
				return false;
			}
		}
		
		//all the newTasks can be removed
		for(Task task : newTasks){
			this.tasks.remove(task);
		}
		
		return true;
	}
	
}
