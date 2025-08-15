package com.ii.smartgrid.smartgrid.model;

import java.util.ArrayList;
import java.util.List;

public class Owner extends CustomObject{
	
	private List<String> smartBuildingNames;
	
    public Owner(){
        super();
        smartBuildingNames = new ArrayList<>();
    }

	public void setSmartBuildingNames(List<String> smartBuildingNames) {
        this.smartBuildingNames = smartBuildingNames;
    }

    public List<String> getSmartBuildingNames() {
		return smartBuildingNames;
	}

	@Override
	public String toString() {
		return "Owner [smartBuildingNames=" + smartBuildingNames + "]";
	}
}
