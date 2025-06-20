package com.ii.smartgrid.smartgrid.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.util.Objects;

@JsonTypeInfo(use = Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type")

@JsonSubTypes({
    @Type(value = Television.class),
    @Type(value = WashingMachine.class)
})
public abstract class Appliance {
	protected boolean alwaysOn;	
	protected boolean on;
	protected double hourlyConsumption;
	
	protected Appliance() {
		super();
	}
	
	protected Appliance(double hourlyConsumption, boolean alwaysOn) {
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Appliance other = (Appliance) obj;
		return alwaysOn == other.alwaysOn
				&& Double.doubleToLongBits(hourlyConsumption) == Double.doubleToLongBits(other.hourlyConsumption)
				&& on == other.on;
	}

	@Override
	public String toString() {
		return "Appliance [alwaysOn=" + alwaysOn + ", on=" + on + ", hourlyConsumption=" + hourlyConsumption + "]";
	}
	
	
	
}
