package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.model.PowerPlant.PPStatus;
import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendProducedEnergyToLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendProducedWindEnergyToLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;
import com.ii.smartgrid.smartgrid.model.WindPowerPlant;

public class WindPowerPlantAgent extends RenewablePowerPlantAgent{

    @Override
    public void setup(){
        String windPowerPlantName = this.getLocalName();

        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.WIND_POWERPLANTS_PATH, windPowerPlantName, WindPowerPlant.class);

        this.referencedObject.addConnectedAgentName(this.getRenewablePowerPlant().getLoadManagerName());
        
        this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new RenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }

    protected SendProducedEnergyToLoadManagerBehaviour createSendEnergyBehaviourBehaviour(){
        return new SendProducedWindEnergyToLoadManagerBehaviour(this);
    }

}