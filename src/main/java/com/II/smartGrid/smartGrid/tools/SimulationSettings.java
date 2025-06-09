package com.II.smartGrid.smartGrid.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.II.smartGrid.smartGrid.model.TimeUtils;

import jade.core.Agent;

public class SimulationSettings extends Agent{
	
	private int nTurns;
	private int turnDuration;
	private int curTurn = 0;
	private List<String> agentNames;

	@Override
    public void setup() {   
		this.turnDuration = TimeUtils.getTurnDuration();
        this.nTurns = 1440 / (this.turnDuration);
        
        agentNames = new ArrayList<String>();
		Object[] args = this.getArguments();
		for(int i = 0; i < args.length; i++) {
			agentNames.add((String) args[i]);
		}
        
        addBehaviour(new StartNewTurn(this));  
        
	}

	public List<String> getAgentNames() {
		return agentNames;
	}

	public int getnTurns() {
		return nTurns;
	}

	public int getTurnDuration() {
		return turnDuration;
	}

	public int getCurTurn() {
		return curTurn;
	}

	public void updateTurn() {
		this.curTurn++;
		
	}
}
