package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;

import com.ii.smartgrid.smartgrid.model.Battery;

public class HydroPowerPlant extends RenewablePowerPlant{
    
    private double hourlyProductionSunny;
	private double hourlyProductionRainy;
	private double hourlyProductionCloudy;

    //                 | CALM | MODERATEBREEZE   | GALE | HURRICANE |
    // | **Sunny**     | 95%  | 95%              | 95%  | 90%       |
    // | **Cloudy**    | 96%  | 96%              | 96%  | 90%       |
    // | **Rainy**     | 120% | 120%             | 120% | 115%      |

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