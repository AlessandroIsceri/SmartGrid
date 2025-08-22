package com.ii.smartgrid.behaviours.powerplant;

import com.ii.smartgrid.agents.PhotovoltaicPowerPlantAgent;
import com.ii.smartgrid.model.entities.RenewablePowerPlant;
import com.ii.smartgrid.utils.EnergyMonitorUtil;
import com.ii.smartgrid.utils.WeatherUtil.WeatherStatus;

public class SendProducedPhotovoltaicEnergyToGridBehaviour extends SendProducedEnergyToGridBehaviour{

    public SendProducedPhotovoltaicEnergyToGridBehaviour(PhotovoltaicPowerPlantAgent photovoltaicPowerPlantAgent){
        super(photovoltaicPowerPlantAgent);
    }
    
    @Override
    protected double getHourlyProduction(RenewablePowerPlant photovoltaicPowerPlant){
        WeatherStatus curWeather = customAgent.getCurWeather();
        int curTurn = customAgent.getCurTurn();
        double curTurnProduction = photovoltaicPowerPlant.getHourlyProduction(curWeather, curTurn);
        EnergyMonitorUtil.addPhotovoltaicRenewableEnergyProduction(curTurnProduction, renewablePowerPlantAgent.getCurTurn());
        return curTurnProduction;
    }
    
}