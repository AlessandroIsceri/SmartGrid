package com.II.smartGrid.smartGrid.agents;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.II.smartGrid.smartGrid.agents.PowerPlant.Status;
import com.II.smartGrid.smartGrid.behaviours.CheckOwnerMessages;
import com.II.smartGrid.smartGrid.behaviours.PowerPlantDistributeEnergy;
import com.II.smartGrid.smartGrid.behaviours.ProduceEnergy;
import com.II.smartGrid.smartGrid.model.Appliance;
import com.II.smartGrid.smartGrid.model.EnergyProducer;
import com.II.smartGrid.smartGrid.model.Routine;
import com.II.smartGrid.smartGrid.model.Task;
import com.II.smartGrid.smartGrid.model.Television;
import com.II.smartGrid.smartGrid.model.WashingMachine;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.AMSService;

import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;

public class Owner extends Agent{
	
	private List<String> smartHomesNames;
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
    public void setup() {
		smartHomesNames = new ArrayList<String>();
		Object[] args = this.getArguments();
		for(int i = 0; i < args.length; i++) {
			smartHomesNames.add((String) args[i]);
		}		
        /*
         * setta una routine del tipo:
         * 10:00-12:00 lavatrice
         * 13:00-14:00 lavastoviglie
         * */
        
		addBehaviour(new CheckOwnerMessages(this));
		
    }

	public List<String> getSmartHomesNames() {
		return smartHomesNames;
	}

	@Override
	public String toString() {
		return "Owner [smartHomesNames=" + smartHomesNames + ", objectMapper=" + objectMapper + "]";
	}
}
