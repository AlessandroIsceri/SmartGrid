package com.ii.smartgrid.smartgrid.model;

import java.util.Objects;

public class Task {
	private Appliance appliance;
	private String startTime;
	private String endTime;
	
	public Task() {
		super();
	}
	
	public Appliance getAppliance() {
		return appliance;
	}
	
	public void setAppliance(Appliance appliance) {
		this.appliance = appliance;
	}
	
	public String getStartTime() {
		return startTime;
	}
	
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
	public String getEndTime() {
		return endTime;
	}
	
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Task(Appliance appliance, String startTime, String endTime) {
		super();
		this.appliance = appliance;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Task other = (Task) obj;
		return Objects.equals(appliance, other.appliance) && Objects.equals(endTime, other.endTime)
				&& Objects.equals(startTime, other.startTime);
	}

	@Override
	public String toString() {
		return "Task [appliance=" + appliance + ", startTime=" + startTime + ", endTime=" + endTime + "]";
	}
	
	
	
}
