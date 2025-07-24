package com.ii.smartgrid.smartgrid.model;

import java.util.ArrayList;

import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.ReceiveNonRenewableEnergyRequestFromLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendNonRenewableEnergyToLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.model.Battery;

import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public abstract class NonRenewablePowerPlant extends PowerPlant{

    protected double hourlyProduction;
    protected double requestedEnergy;

    public NonRenewablePowerPlant() {
        super();
    }

    public void setHourlyProduction(double hourlyProduction) {
        this.hourlyProduction = hourlyProduction;
    }

    public double getHourlyProduction() {
        return hourlyProduction;
    }

    public double getRequestedEnergy() {
        return requestedEnergy;
    }

    public void setRequestedEnergy(double requestedEnergy) {
        this.requestedEnergy = requestedEnergy;
    }



    


}
