package com.ii.smartgrid.smartgrid.agents;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.PowerPlant.PPStatus;
import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.PowerPlantDistributeEnergy;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.ProduceEnergy;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.EditRoutine;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.FollowRoutine;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.ManageEnergy;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.WaitForRestoreBehaviour;
import com.ii.smartgrid.smartgrid.model.Appliance;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.EnergyProducer;
import com.ii.smartgrid.smartgrid.model.Routine;
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
	private String gridName;

	private enum SmartHomeStatus {BLACKOUT, WORKING};
	private SmartHomeStatus status = SmartHomeStatus.WORKING;
	
	
	@Override
    public void setup() {    
        File from = new File((String) this.getArguments()[0]); 
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        
        mapper = new ObjectMapper();
        
        try {
			HashMap<String, Object> jsonObject = mapper.readValue(from, typeRef);

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
        		expectedConsumption = expectedConsumption + appliances.get(i).gethConsumption() / 60 * TimeUtils.getTurnDuration();
        	}
        	
        }
        
        this.log("Setup completed");

        
        //richiede energia se serve (non ne ha abbastanza, attaccandosi alla rete)
        //puo' rilasciare energia se ne ha troppa e non gli serve
        //spegnere/accendere gli elettrodomestici in base alla routine decisa dall'owner
            
        this.addBehaviour(new SmartHomeTurnBehaviour(this));
        addBehaviour(new EditRoutine(this));
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
                expectedConsumption += appliance.gethConsumption() / 60 * TimeUtils.getTurnDuration();
			}
		}
        status = SmartHomeStatus.WORKING;
	}

    public SmartHomeStatus getStatus() {
        return status;
    }

    private class SmartHomeTurnBehaviour extends GenericTurnBehaviour{

        private SmartHomeTurnBehaviour(SmartHome smartHome){
            super(smartHome);
        }

        @Override
        protected void executeTurn(ACLMessage replyMsg, SequentialBehaviour sequentialTurnBehaviour) {
            log("" + ((SmartHome) myAgent).getStatus());
            if(((SmartHome) myAgent).getStatus() == SmartHomeStatus.WORKING){
                sequentialTurnBehaviour.addSubBehaviour(new FollowRoutine((SmartHome) myAgent));
                sequentialTurnBehaviour.addSubBehaviour(new ManageEnergy((SmartHome) myAgent));
            }
            else if(((SmartHome) myAgent).getStatus() == SmartHomeStatus.BLACKOUT){
				log("else (status blackout)");
                sequentialTurnBehaviour.addSubBehaviour(new WaitForRestoreBehaviour((SmartHome) myAgent));
            }
        }
    }
}
