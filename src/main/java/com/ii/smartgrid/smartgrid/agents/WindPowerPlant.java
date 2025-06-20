package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;

import com.ii.smartgrid.smartgrid.model.Battery;

public class WindPowerPlant extends RenewablePowerPlant{


    private double hourlyProductionCalm;
	private double hourlyProductionModerateBreeze;
    private double hourlyProductionGale;
	private double hourlyProductionHurricane;

    @Override
    public void setup(){
        status = PPStatus.ON;
        Object[] args = this.getArguments();
        
        loadManagerName = (String) args[0];
        hourlyProductionCalm = Double.parseDouble((String) args[1]);
        hourlyProductionModerateBreeze = Double.parseDouble((String) args[2]);
        hourlyProductionGale = Double.parseDouble((String) args[3]);
        hourlyProductionHurricane = Double.parseDouble((String) args[4]);
        double maxCapacity = Double.parseDouble((String) args[5]);
        double storedEnergy = Double.parseDouble((String) args[6]);
        
        battery = new Battery(maxCapacity, storedEnergy);
        addBehaviour(new RenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }

    
    //Scala di Beaufort


    @Override
    public double getHourlyProduction() {
		switch(curWindSpeed) {
			case CALM:
				return hourlyProductionCalm;
			case MODERATE_BREEZE:
				return hourlyProductionModerateBreeze;
			case GALE:
				return hourlyProductionGale;
            case HURRICANE:
                return hourlyProductionHurricane;
			default:
                log("Error: WindSpeed not found");
				return 0;
		}
	}   

}