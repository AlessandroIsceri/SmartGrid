package com.ii.smartgrid.model.entities;

import java.util.ArrayList;
import java.util.List;

public class Owner extends CustomObject {

    private List<String> smartBuildingNames;

    public Owner() {
        super();
        smartBuildingNames = new ArrayList<>();
    }

    public List<String> getSmartBuildingNames() {
        return smartBuildingNames;
    }

    public void setSmartBuildingNames(List<String> smartBuildingNames) {
        this.smartBuildingNames = smartBuildingNames;
    }

    @Override
    public String toString() {
        return "Owner [smartBuildingNames=" + smartBuildingNames + "]";
    }
}
