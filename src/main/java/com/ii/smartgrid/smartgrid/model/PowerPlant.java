package com.ii.smartgrid.smartgrid.model;

import java.util.ArrayList;
import java.util.List;

import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.model.Battery;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public abstract class PowerPlant extends CustomObject{
	
	public enum PPStatus {ON, OFF, MAINTENANCE};
	protected PPStatus status;
    protected String loadManagerName;
	protected Battery battery;
    protected double curTurnExpectedProduction;


    public abstract double getHourlyProduction(Object... weatherConditions);


	public PPStatus getStatus() {
		return status;
	}

	public Battery getBattery() {
        return battery;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
    }

    public double getCurTurnExpectedProduction() {
        return curTurnExpectedProduction;
    }

    public void setCurTurnExpectedProduction(double curTurnExpectedProduction) {
        this.curTurnExpectedProduction = curTurnExpectedProduction;
    }

	public void setStatus(PPStatus status) {
		this.status = status;
	}

    public String getLoadManagerName() {
        return loadManagerName;
    }

    @Override
    public String toString() {
        return "PowerPlant [status=" + status + ", loadManagerName=" + loadManagerName + ", battery=" + battery
                + ", curTurnExpectedProduction=" + curTurnExpectedProduction + "]";
    }	
}

