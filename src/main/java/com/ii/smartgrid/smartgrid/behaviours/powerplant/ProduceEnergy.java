package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import com.ii.smartgrid.smartgrid.agents.PowerPlant;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

public class ProduceEnergy extends CyclicBehaviour{

	private PowerPlant agent;
	
	public ProduceEnergy(PowerPlant agent) {
		this.agent = agent;
	}
	
	@Override
	public void action() {
		// TODO Auto-generated method stub
		agent.setStoredEnergy(agent.getStoredEnergy() + agent.getHProduction());
	}

}
