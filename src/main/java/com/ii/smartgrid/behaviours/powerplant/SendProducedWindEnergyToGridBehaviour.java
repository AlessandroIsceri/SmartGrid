package com.ii.smartgrid.behaviours.powerplant;

import com.ii.smartgrid.agents.WindPowerPlantAgent;
import com.ii.smartgrid.model.entities.RenewablePowerPlant;
import com.ii.smartgrid.utils.EnergyMonitorUtil;

public class SendProducedWindEnergyToGridBehaviour extends SendProducedEnergyToGridBehaviour{

    public SendProducedWindEnergyToGridBehaviour(WindPowerPlantAgent windPowerPlantAgent){
        super(windPowerPlantAgent);
    }
    
    @Override
    protected double getHourlyProduction(RenewablePowerPlant windPowerPlant){
        double curTurnProduction = windPowerPlant.getHourlyProduction(customAgent.getCurWindSpeed());
        EnergyMonitorUtil.addWindRenewableEnergyProduction(curTurnProduction, renewablePowerPlantAgent.getCurTurn());
        return curTurnProduction;
    }
    
}
