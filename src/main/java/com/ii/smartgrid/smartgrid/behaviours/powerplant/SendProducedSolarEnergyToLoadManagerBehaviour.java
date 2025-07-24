package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SolarPowerPlantAgent;
import com.ii.smartgrid.smartgrid.model.RenewablePowerPlant;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;

public class SendProducedSolarEnergyToLoadManagerBehaviour extends SendProducedEnergyToLoadManagerBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public SendProducedSolarEnergyToLoadManagerBehaviour(SolarPowerPlantAgent solarPowerPlantAgent){
        super(solarPowerPlantAgent);
    }
    
    @Override
    protected double getHourlyProduction(RenewablePowerPlant solarPowerPlant){
        WeatherStatus curWeather = ((CustomAgent) myAgent).getCurWeather();
        int curTurn = ((CustomAgent) myAgent).getCurTurn();
        return solarPowerPlant.getHourlyProduction(curWeather, curTurn);
    }
    
}