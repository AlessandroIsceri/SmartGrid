package com.ii.smartgrid.smartgrid.agents;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.ii.smartgrid.smartgrid.behaviours.owner.CheckOwnerMessagesBehaviour;
import com.ii.smartgrid.smartgrid.model.Appliance;
import com.ii.smartgrid.smartgrid.model.HydroPowerPlant;
import com.ii.smartgrid.smartgrid.model.Owner;
import com.ii.smartgrid.smartgrid.model.Routine;
import com.ii.smartgrid.smartgrid.model.Task;
import com.ii.smartgrid.smartgrid.model.Television;
import com.ii.smartgrid.smartgrid.model.WashingMachine;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.AMSService;

public class OwnerAgent extends CustomAgent{
	
	// private Owner owner;
	
    public Owner getOwner(){
        return (Owner) this.referencedObject;
    }

	@Override
    public void setup() {
        String owner = this.getLocalName();
        log(JsonUtil.OWNERS_PATH);
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.OWNERS_PATH, owner, Owner.class);
        
        this.log("Setup completed");
		this.addBehaviour(new CheckOwnerMessagesBehaviour(this));
		
    }
}
