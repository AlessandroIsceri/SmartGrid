package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.behaviours.powerplant.NonRenewablePowerPlantCoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.model.DieselPowerPlant;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlant;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

public class DieselPowerPlantAgent extends NonRenewablePowerPlantAgent{
        
    @Override
    public void setup(){
        String dieselPowerPlantName = this.getLocalName();

        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.DIESEL_POWERPLANTS_PATH, dieselPowerPlantName, DieselPowerPlant.class);
        
        NonRenewablePowerPlant dieselPowerPlant = getNonRenewablePowerPlant();
        dieselPowerPlant.setOn(false);

        dieselPowerPlant.addConnectedAgentName(dieselPowerPlant.getGridName());

        this.addBehaviour(new NonRenewablePowerPlantCoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new NonRenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }
    
}
