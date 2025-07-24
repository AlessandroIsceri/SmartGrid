package com.ii.smartgrid.smartgrid.agents;

import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.DieselPowerPlant;
import com.ii.smartgrid.smartgrid.model.PowerPlant.PPStatus;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

public class DieselPowerPlantAgent extends NonRenewablePowerPlantAgent{
    
    // private DieselPowerPlant dieselPowerPlant;
    
    public void setup(){
        
        String dieselPowerPlantName = this.getLocalName();

        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.DIESEL_POWERPLANTS_PATH, dieselPowerPlantName, DieselPowerPlant.class);
        ((DieselPowerPlant) this.referencedObject).setStatus(PPStatus.ON);

        this.referencedObject.addConnectedAgentName(this.getNonRenewablePowerPlant().getLoadManagerName());
        
        this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new NonRenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }
    
}
