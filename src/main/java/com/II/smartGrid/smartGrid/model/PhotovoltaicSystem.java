package com.II.smartGrid.smartGrid.model;

public class PhotovoltaicSystem implements EnergyProducer{
	private double hProduction;
	
	public double getHProduction() {
		return hProduction;
	}

	@Override
	public String toString() {
		return "PhotovoltaicSystem [hProduction=" + hProduction + "]";
	}
	
}
