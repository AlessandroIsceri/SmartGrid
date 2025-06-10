package com.II.smartGrid.smartGrid.behaviours;

import java.util.List;

import com.II.smartGrid.smartGrid.agents.SmartHome;
import com.II.smartGrid.smartGrid.model.EnergyProducer;
import com.II.smartGrid.smartGrid.model.TimeUtils;
import com.II.smartGrid.smartGrid.tools.SimulationSettings.WeatherStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.behaviours.CyclicBehaviour;

public class ManageEnergy extends CyclicBehaviour{


	private ObjectMapper objectMapper = new ObjectMapper();
	
	public ManageEnergy(SmartHome smartHome) {
		super(smartHome);
	}

	@Override
	public void action() {
		//richiede energia se serve (non ne ha abbastanza, attaccandosi alla rete)
        //può rilasciare energia se ne ha troppa e non gli serve
		double expectedConsumption = ((SmartHome) myAgent).getExpectedConsumption();
		double storedEnergy = ((SmartHome) myAgent).getStoredEnergy();
		
		List<EnergyProducer> energyProducers = ((SmartHome) myAgent).getEnergyProducers();
		
		//0:15 * 10 = 150 / 60 = 2
		int hour = (TimeUtils.getTurnDuration() * ((SmartHome) myAgent).getCurTurn()) / 60;
		WeatherStatus curWeatherStatus = ((SmartHome) myAgent).getCurWeatherStatus();
		for(int i = 0; i < energyProducers.size(); i++) {
			energyProducers.get(i).getHProduction(curWeatherStatus, hour);
		}
		
		
	}

}
