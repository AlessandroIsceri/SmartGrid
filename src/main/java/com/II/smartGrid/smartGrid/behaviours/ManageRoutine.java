package com.II.smartGrid.smartGrid.behaviours;

import java.util.List;

import com.II.smartGrid.smartGrid.agents.SmartHome;
import com.II.smartGrid.smartGrid.model.Appliance;
import com.II.smartGrid.smartGrid.model.Routine;
import com.II.smartGrid.smartGrid.model.Task;
import com.II.smartGrid.smartGrid.model.TimeUtils;
import com.II.smartGrid.smartGrid.tools.SimulationSettings;

import jade.core.behaviours.CyclicBehaviour;

public class ManageRoutine extends CyclicBehaviour {	
	
	public ManageRoutine(SmartHome smartHome) {
		super(smartHome);
	}
	
	@Override
	public void action() {
		List<Appliance> appliances = ((SmartHome) myAgent).getAppliances();	
		Routine routine =  ((SmartHome) myAgent).getRoutine();
		List<Task> tasks = routine.getTasks();
		
		int curTurn = SimulationSettings.getInstance().getCurTurn();
		
		for(int i = 0; i < tasks.size(); i++) {
			Task curTask = tasks.get(i);
			int startTurn = TimeUtils.convertTimeToTurn(curTask.getStartTime());
			int endTurn = TimeUtils.convertTimeToTurn(curTask.getEndTime());
			if(startTurn == curTurn) {
				curTask.getAppliance().setOn(true);
			} else if(endTurn == curTurn) {
				curTask.getAppliance().setOn(false);
			}
		}
		
		block();
		
	}
}
