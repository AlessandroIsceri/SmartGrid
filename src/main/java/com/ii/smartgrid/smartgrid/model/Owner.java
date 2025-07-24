package com.ii.smartgrid.smartgrid.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.ii.smartgrid.smartgrid.model.PowerPlant.PPStatus;
import com.ii.smartgrid.smartgrid.behaviours.owner.CheckOwnerMessagesBehaviour;
import com.ii.smartgrid.smartgrid.model.Appliance;
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

public class Owner extends CustomObject{
	
	private List<String> smartHomeNames;
	
    public Owner(){
        super();
    }

	public void setSmartHomeNames(List<String> smartHomeNames) {
        this.smartHomeNames = smartHomeNames;
    }

    public List<String> getSmartHomeNames() {
		return smartHomeNames;
	}

	@Override
	public String toString() {
		return "Owner [smartHomeNames=" + smartHomeNames + "]";
	}
}
