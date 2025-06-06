package com.II.smartGrid.smartGrid.agents;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.II.smartGrid.smartGrid.agents.PowerPlant.Status;
import com.II.smartGrid.smartGrid.behaviours.CheckSmartHomeMessages;
import com.II.smartGrid.smartGrid.behaviours.ManageRoutine;
import com.II.smartGrid.smartGrid.behaviours.PowerPlantDistributeEnergy;
import com.II.smartGrid.smartGrid.behaviours.ProduceEnergy;
import com.II.smartGrid.smartGrid.model.Appliance;
import com.II.smartGrid.smartGrid.model.EnergyProducer;
import com.II.smartGrid.smartGrid.model.Routine;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;

public class SmartHome extends Agent{
	private List<Appliance> appliances;
	private List<EnergyProducer> energyProducers;
	private double maxPower;
	private double minEnergyConsumption;
	private double maxCapacity;
	private double storedEnergy;
	private Routine routine;
	private ObjectMapper mapper;
	
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
			minEnergyConsumption = (double) jsonObject.get("minEnergyConsumption");
			maxCapacity = (double) jsonObject.get("maxCapacity");
			storedEnergy = (double) jsonObject.get("storedEnergy");
			routine = mapper.convertValue(jsonObject.get("routine"), Routine.class);
			
			//System.out.println(this.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        
        for(int i = 0; i < appliances.size(); i++) {
        	if(appliances.get(i).isAlwaysOn() == true) {
        		minEnergyConsumption = minEnergyConsumption + appliances.get(i).gethConsumption();
        	}
        	
        }
        
        //richiede energia se serve (non ne ha abbastanza, attaccandosi alla rete)
        //spegnere/accendere gli elettrodomestici in base alla routine decisa dall'owner
        //può rilasciare energia se ne ha troppa e non gli serve
        
        addBehaviour(new CheckSmartHomeMessages(this));
        
        ParallelBehaviour p = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
        p.addSubBehaviour(new ManageRoutine(this));
        //.addSubBehaviour(new );
        
        
        
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

	public double getMinEnergyConsumption() {
		return minEnergyConsumption;
	}

	public void setMinEnergyConsumption(double minEnergyConsumption) {
		this.minEnergyConsumption = minEnergyConsumption;
	}

	public double getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(double maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public double getStoredEnergy() {
		return storedEnergy;
	}

	public void setStoredEnergy(double storedEnergy) {
		this.storedEnergy = storedEnergy;
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

	
	@Override
	public String toString() {
		return "SmartHome [appliances=" + appliances + ", energyProducers=" + energyProducers + ", maxPower=" + maxPower
				+ ", minEnergyConsumption=" + minEnergyConsumption + ", maxCapacity=" + maxCapacity + ", storedEnergy="
				+ storedEnergy + ", routine=" + routine + "]";
	}
	
}
