package com.ii.smartgrid.smartgrid.agents;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.PowerPlant.PPStatus;
import com.ii.smartgrid.smartgrid.behaviours.owner.CheckOwnerMessagesBehaviour;
import com.ii.smartgrid.smartgrid.model.Appliance;
import com.ii.smartgrid.smartgrid.model.EnergyProducer;
import com.ii.smartgrid.smartgrid.model.Routine;
import com.ii.smartgrid.smartgrid.model.Task;
import com.ii.smartgrid.smartgrid.model.Television;
import com.ii.smartgrid.smartgrid.model.WashingMachine;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.AMSService;

public class Owner extends CustomAgent{
	
	private List<String> smartHomeNames;
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
    public void setup() {
		smartHomeNames = new ArrayList<String>();
		Object[] args = this.getArguments();
		for(int i = 0; i < args.length; i++) {
			smartHomeNames.add((String) args[i]);
		}		
        /*
         * setta una routine del tipo:
         * 10:00-12:00 lavatrice
         * 13:00-14:00 lavastoviglie
         * */
        
        this.log("Setup completed");
		addBehaviour(new CheckOwnerMessagesBehaviour(this));
		
    }

	public List<String> getSmartHomeNames() {
		return smartHomeNames;
	}

	@Override
	public String toString() {
		return "Owner [smartHomeNames=" + smartHomeNames + ", objectMapper=" + objectMapper + "]";
	}
}
