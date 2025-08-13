package com.ii.smartgrid.smartgrid.model;

import java.util.ArrayList;
import java.util.List;

public class Owner extends CustomObject{
	
	private List<String> smartHomeNames;
	
    public Owner(){
        super();
        smartHomeNames = new ArrayList<String>();
    }

	public void setSmartHomeNames(List<String> smartHomeNames) {
        this.smartHomeNames = smartHomeNames;
    }

    public List<String> getSmartHomeNames() {
		return smartHomeNames;
	}

	@Override
	public String toString() {
		return "Owner [smartHomeNames=" + smartHomeNames + "]";
	}
}
