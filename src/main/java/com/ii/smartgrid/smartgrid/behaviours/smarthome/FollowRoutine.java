package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.util.HashMap;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.SmartHome;
import com.ii.smartgrid.smartgrid.model.Appliance;
import com.ii.smartgrid.smartgrid.model.Routine;
import com.ii.smartgrid.smartgrid.model.Task;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WeatherStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class FollowRoutine extends OneShotBehaviour {	
	public FollowRoutine(SmartHome smartHome) {
		super(smartHome);
	}
	
	@Override
	public void action() {
		((SmartHome) myAgent).log("FOLLOW ROUTINE STARTED");
		List<Appliance> appliances = ((SmartHome) myAgent).getAppliances();	
		Routine routine =  ((SmartHome) myAgent).getRoutine();
		List<Task> tasks = routine.getTasks();
		
		int curTurn = ((SmartHome) myAgent).getCurTurn();
		WeatherStatus weather = ((SmartHome) myAgent).getCurWeather();
		double expectedConsumption = ((SmartHome) myAgent).getExpectedConsumption();
		int turnDuration = TimeUtils.getTurnDuration();
		
		for(int i = 0; i < tasks.size(); i++) {
			Task curTask = tasks.get(i);
			int startTurn = TimeUtils.convertTimeToTurn(curTask.getStartTime());
			int endTurn = TimeUtils.convertTimeToTurn(curTask.getEndTime());
			if(startTurn == curTurn) {
				curTask.getAppliance().setOn(true);
				expectedConsumption += curTask.getAppliance().gethConsumption() / 60 * turnDuration;
				((SmartHome) myAgent).log("Turn dur: " + turnDuration + " - " + "adding consumption: " + curTask.getAppliance().gethConsumption());
			} else if(endTurn == curTurn) {
				curTask.getAppliance().setOn(false);
				expectedConsumption -= curTask.getAppliance().gethConsumption() / 60 * turnDuration;
				((SmartHome) myAgent).log("Turn dur: " + turnDuration + " - " + "removing consumption: " + curTask.getAppliance().gethConsumption());
			}
		}

        ((SmartHome) myAgent).log("expectedConsumption: " + expectedConsumption);
        ((SmartHome) myAgent).setExpectedConsumption(expectedConsumption);
        ((SmartHome) myAgent).log("FOLLOW ROUTINE FINISHED");
	}

}
