package com.ii.smartgrid.smartgrid.agents;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.PowerPlant.PPStatus;
import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.CheckSmartHomeMessagesBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.ReceiveEnergyFromGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.SendEnergyRequestToGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.SendEnergyToGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.WaitForRestoreBehaviour;
import com.ii.smartgrid.smartgrid.model.Appliance;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.EnergyProducer;
import com.ii.smartgrid.smartgrid.model.Routine;
import com.ii.smartgrid.smartgrid.model.Task;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public class SmartHome extends CustomAgent{
	private List<Appliance> appliances;
	private List<EnergyProducer> energyProducers;
	private Battery battery;
	private double maxPower;
	private Routine routine;
	private ObjectMapper mapper;
	private double expectedConsumption;
    private double expectedProduction;
	private String gridName;

	private enum SmartHomeStatus {BLACKOUT, WORKING};
	private SmartHomeStatus status = SmartHomeStatus.WORKING;
	
	@Override
    public void setup() {    
        File from = new File((String) this.getArguments()[0]); 
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        //TODO REFACTOR THIS PART
        mapper = new ObjectMapper();
        
        try {
			Map<String, Object> jsonObject = mapper.readValue(from, typeRef);

			//appliances = (ArrayList<Appliance>) jsonObject.get("appliances");
			appliances = mapper.convertValue(jsonObject.get("appliances"), new TypeReference<List<Appliance>>() { });
			//energyProducers = (ArrayList<EnergyProducer>) jsonObject.get("energyProducers");
			energyProducers = mapper.convertValue(jsonObject.get("energyProducers"), new TypeReference<List<EnergyProducer>>() { });
			maxPower = (double) jsonObject.get("maxPower");
			battery = mapper.convertValue(jsonObject.get("battery"), Battery.class);
			routine = mapper.convertValue(jsonObject.get("routine"), Routine.class);
			gridName = (String) jsonObject.get("gridName");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        
        for(int i = 0; i < appliances.size(); i++) {
        	if(appliances.get(i).isAlwaysOn() == true) {
        		expectedConsumption = expectedConsumption + appliances.get(i).getHourlyConsumption() / 60 * TimeUtils.getTurnDuration();
        	}
        	
        }
        
        this.log("Setup completed");

        
        //richiede energia se serve (non ne ha abbastanza, attaccandosi alla rete)
        //puo' rilasciare energia se ne ha troppa e non gli serve
        //spegnere/accendere gli elettrodomestici in base alla routine decisa dall'owner
            
        this.addBehaviour(new SmartHomeTurnBehaviour(this));
        this.addBehaviour(new CheckSmartHomeMessagesBehaviour(this));
    }

	public List<Appliance> getAppliances() {
		return appliances;
	}

	public List<EnergyProducer> getEnergyProducers() {
		return energyProducers;
	}

	public void setEnergyProducers(List<EnergyProducer> energyProducers) {
		this.energyProducers = energyProducers;
	}

	public double getMaxPower() {
		return maxPower;
	}

	public void setMaxPower(double maxPower) {
		this.maxPower = maxPower;
	}

	public Routine getRoutine() {
		return routine;
	}

	public void setRoutine(Routine routine) {
		this.routine = routine;
	}

	public void setAppliances(List<Appliance> appliances) {
		this.appliances = appliances;
	}
	
	public double getExpectedConsumption() {
		return expectedConsumption;
	}

	public void setExpectedConsumption(double expectedConsumption) {
		this.expectedConsumption = expectedConsumption;
	}

	public Battery getBattery() {
		return battery;
	}

	public void setBattery(Battery battery) {
		this.battery = battery;
	}
	
	public String getGridName() {
		return gridName;
	}

	public void setGridName(String gridName) {
		this.gridName = gridName;
	}

	@Override
	public String toString() {
		return "SmartHome [appliances=" + appliances + ", energyProducers=" + energyProducers + ", battery=" + battery
				+ ", maxPower=" + maxPower + ", routine=" + routine + ", mapper=" + mapper + ", expectedConsumption="
				+ expectedConsumption + "]";
	}

	public double getAvailableEnergy(){
        if(battery != null){
            return expectedProduction += battery.getStoredEnergy();
        }
		return expectedProduction;
	}


	public double getExpectedProduction() {
        return expectedProduction;
    }

    public void setExpectedProduction(double expectedProduction) {
        this.expectedProduction = expectedProduction;
    }

    public void shutDown(){
		for(Appliance appliance: appliances){
			appliance.setOn(false);
		}
        expectedConsumption = 0;
        status = SmartHomeStatus.BLACKOUT;
    }

	public void restorePower(double energy){
		if(battery != null){
			double extra = battery.fillBattery(energy);
		}
		for(Appliance appliance: appliances){
			if(appliance.isAlwaysOn()){
				appliance.setOn(true);
                expectedConsumption += appliance.getHourlyConsumption() / 60 * TimeUtils.getTurnDuration();
			}
		}
        status = SmartHomeStatus.WORKING;
	}

    public SmartHomeStatus getStatus() {
        return status;
    }

    public void followRoutine(){
        expectedConsumption = 0;
		expectedProduction = 0;

		int turnDuration = TimeUtils.getTurnDuration();
		for(Task curTask : routine.getTasks()) {
			int startTurn = TimeUtils.convertTimeToTurn(curTask.getStartTime());
			int endTurn = TimeUtils.convertTimeToTurn(curTask.getEndTime());
			if(startTurn == curTurn) {
				curTask.getAppliance().setOn(true);
				expectedConsumption += curTask.getAppliance().getHourlyConsumption() / 60 * turnDuration;
				log("Turn dur: " + turnDuration + " - " + "adding consumption: " + curTask.getAppliance().getHourlyConsumption());
			} else if(endTurn == curTurn) {
				curTask.getAppliance().setOn(false);
				expectedConsumption -= curTask.getAppliance().getHourlyConsumption() / 60 * turnDuration;
				log("Turn dur: " + turnDuration + " - " + "removing consumption: " + curTask.getAppliance().getHourlyConsumption());
			}
		}

        int hour = TimeUtils.getHourFromTurn(curTurn);
		
		for(EnergyProducer energyProducer: energyProducers) {
			expectedProduction += energyProducer.getHourlyProduction(curWeather, hour) / 60 * TimeUtils.getTurnDuration();
		}
		
        log("expectedProduction: " + expectedProduction);
        log("expectedConsumption: " + expectedConsumption);
    }

    private class SmartHomeTurnBehaviour extends GenericTurnBehaviour{

        private SmartHomeTurnBehaviour(SmartHome smartHome){
            super(smartHome);
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            log("home status: " + ((SmartHome) myAgent).getStatus());
            if(((SmartHome) myAgent).getStatus() == SmartHomeStatus.WORKING){
                
				((SmartHome) myAgent).followRoutine();
			
                double availableEnergy = ((SmartHome) myAgent).getAvailableEnergy();
				double expectedConsumption = ((SmartHome) myAgent).getExpectedConsumption();

                if(expectedConsumption > availableEnergy){
                    sequentialTurnBehaviour.addSubBehaviour(new SendEnergyRequestToGridBehaviour((SmartHome) myAgent));
                    sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyFromGridBehaviour((SmartHome) myAgent));
                }else{
                    sequentialTurnBehaviour.addSubBehaviour(new SendEnergyToGridBehaviour((SmartHome) myAgent));
                }
                
            }
            else if(((SmartHome) myAgent).getStatus() == SmartHomeStatus.BLACKOUT){
				log("else (status blackout)");
                sequentialTurnBehaviour.addSubBehaviour(new WaitForRestoreBehaviour((SmartHome) myAgent));
            }
        }
    }
}
