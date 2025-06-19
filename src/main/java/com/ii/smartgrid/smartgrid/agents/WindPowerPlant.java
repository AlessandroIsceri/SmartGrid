package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;

import com.ii.smartgrid.smartgrid.model.Battery;

public class WindPowerPlant extends RenewablePowerPlant{


    private double hProductionCalm;
	private double hProductionModerateBreeze;
    private double hProductionGale;
	private double hProductionHurricane;

    @Override
    public void setup(){
        status = PPStatus.ON;
        Object[] args = this.getArguments();
        
        loadManagerName = (String) args[args.length - 7];
        hProductionCalm = Double.parseDouble((String) args[args.length - 6]);
        hProductionModerateBreeze = Double.parseDouble((String) args[args.length - 5]);
        hProductionGale = Double.parseDouble((String) args[args.length - 4]);
        hProductionHurricane = Double.parseDouble((String) args[args.length - 3]);
        double maxCapacity = Double.parseDouble((String) args[args.length - 2]);
        double storedEnergy = Double.parseDouble((String) args[args.length - 1]);
        
        battery = new Battery(maxCapacity, storedEnergy);
        addBehaviour(new RenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }

    
    //Scala di Beaufort


    @Override
    public double getHProduction() {
		switch(curWindSpeed) {
			case CALM:
				return hProductionCalm;
			case MODERATE_BREEZE:
				return hProductionModerateBreeze;
			case GALE:
				return hProductionGale;
            case HURRICANE:
                return hProductionHurricane;
			default:
                log("Error: WindSpeed not found");
				return 0;
		}
	}   

}