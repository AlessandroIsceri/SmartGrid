package com.ii.smartgrid.smartgrid.model;

public class Appliance {
    private String name;
	private boolean alwaysOn;	
	private boolean on;
	private double hourlyConsumption;
	
	private Appliance() {
		super();
	}
	
	private Appliance(double hourlyConsumption, boolean alwaysOn) {
		this.hourlyConsumption = hourlyConsumption;
		this.alwaysOn = alwaysOn;
	}

	public boolean isAlwaysOn() {
		return alwaysOn;
	}

	public void setAlwaysOn(boolean alwaysOn) {
		this.alwaysOn = alwaysOn;
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	public double getHourlyConsumption() {
		return hourlyConsumption;
	}

	public void setHourlyConsumption(double hourlyConsumption) {
		this.hourlyConsumption = hourlyConsumption;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Appliance other = (Appliance) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (alwaysOn != other.alwaysOn)
            return false;
        if (on != other.on)
            return false;
        if (Double.doubleToLongBits(hourlyConsumption) != Double.doubleToLongBits(other.hourlyConsumption))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Appliance [name=" + name + ", alwaysOn=" + alwaysOn + ", on=" + on + ", hourlyConsumption="
                + hourlyConsumption + "]";
    }
    
	
	
}
