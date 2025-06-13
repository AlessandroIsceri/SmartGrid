package com.ii.smartgrid.smartgrid.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Television extends Appliance {
	
	public Television() {
		super();
	}
	
	public Television(double hConsumption, boolean alwaysOn) {
		super(hConsumption, alwaysOn);
	}
}
