package com.ii.smartgrid.smartgrid.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Television extends Appliance {
	
	public Television() {
		super();
	}
	
	public Television(double hourlyConsumption, boolean alwaysOn) {
		super(hourlyConsumption, alwaysOn);
	}
}
