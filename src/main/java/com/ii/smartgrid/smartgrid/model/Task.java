package com.ii.smartgrid.smartgrid.model;

public class Task {
	private String applianceName;
	private String startTime;
	private String endTime;
	
	public Task() {
		super();
	}
	
	public String getApplianceName() {
		return applianceName;
	}
	
	public void setApplianceName(String applianceName) {
		this.applianceName = applianceName;
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

	public Task(String applianceName, String startTime, String endTime) {
		super();
		this.applianceName = applianceName;
		this.startTime = startTime;
		this.endTime = endTime;
	}

    @Override
    public String toString() {
        return "Task [applianceName=" + applianceName + ", startTime=" + startTime + ", endTime=" + endTime + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((applianceName == null) ? 0 : applianceName.hashCode());
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        return result;
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
        if (applianceName == null) {
            if (other.applianceName != null)
                return false;
        } else if (!applianceName.equals(other.applianceName))
            return false;
        if (startTime == null) {
            if (other.startTime != null)
                return false;
        } else if (!startTime.equals(other.startTime))
            return false;
        if (endTime == null) {
            if (other.endTime != null)
                return false;
        } else if (!endTime.equals(other.endTime))
            return false;
        return true;
    }

	
}
