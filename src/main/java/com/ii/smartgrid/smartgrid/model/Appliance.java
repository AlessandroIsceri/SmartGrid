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
	protected double hConsumption;
	
	protected Appliance() {
		super();
	}
	
	protected Appliance(double hConsumption, boolean alwaysOn) {
		this.hConsumption = hConsumption;
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

	public double gethConsumption() {
		return hConsumption;
	}

	public void sethConsumption(double hConsumption) {
		this.hConsumption = hConsumption;
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
				&& Double.doubleToLongBits(hConsumption) == Double.doubleToLongBits(other.hConsumption)
				&& on == other.on;
	}

	@Override
	public String toString() {
		return "Appliance [alwaysOn=" + alwaysOn + ", on=" + on + ", hConsumption=" + hConsumption + "]";
	}
	
	
	
}
