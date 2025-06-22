package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;

import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

public class SolarPowerPlant extends RenewablePowerPlant{

    private double hourlyProductionSunny;
	private double hourlyProductionRainy;
	private double hourlyProductionCloudy;

    @Override
    public void setup(){
        status = PPStatus.ON;
        Object[] args = this.getArguments();
        
        loadManagerName = (String) args[0];
        hourlyProductionSunny = Double.parseDouble((String) args[1]);
        hourlyProductionRainy = Double.parseDouble((String) args[2]);
        hourlyProductionCloudy = Double.parseDouble((String) args[3]);
        double maxCapacity = Double.parseDouble((String) args[4]);
        double storedEnergy = Double.parseDouble((String) args[5]);
        
        battery = new Battery(maxCapacity, storedEnergy);


        addBehaviour(new RenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }

    @Override
    public double getHourlyProduction() {
		//Night time from 20:00  to 6:00
        int hour = TimeUtils.getHourFromTurn(curTurn);
        if(hour < 6 || hour > 19) {
			return 0;
		}
        
		switch(curWeather) {
			case SUNNY:
				return hourlyProductionSunny;
			case RAINY:
				return hourlyProductionRainy;
			case CLOUDY:
				return hourlyProductionCloudy;
			default:
                log("Error: Weather not found");
				return 0;
		}
	}   

}
