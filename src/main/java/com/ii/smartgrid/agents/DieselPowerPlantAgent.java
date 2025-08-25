package com.ii.smartgrid.agents;

import com.ii.smartgrid.behaviours.powerplant.NonRenewablePowerPlantCoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.model.entities.DieselPowerPlant;
import com.ii.smartgrid.model.entities.NonRenewablePowerPlant;
import com.ii.smartgrid.utils.JsonUtil;

public class DieselPowerPlantAgent extends NonRenewablePowerPlantAgent{
        
    @Override
    public void setup(){
        String dieselPowerPlantName = this.getLocalName();

        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.DIESEL_POWER_PLANTS_PATH, dieselPowerPlantName, DieselPowerPlant.class);
        
        NonRenewablePowerPlant dieselPowerPlant = getNonRenewablePowerPlant();

        this.nonRenewablePowerPlantState = NonRenewablePowerPlantState.ON;

        dieselPowerPlant.addConnectedAgentName(dieselPowerPlant.getGridName());
        dieselPowerPlant.setTurnRequest(dieselPowerPlant.getMaxHourlyProduction());

        this.addBehaviour(new NonRenewablePowerPlantCoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new NonRenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }
    
}
