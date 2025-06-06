package com.II.smartGrid.smartGrid.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.II.smartGrid.smartGrid.model.TimeUtils;

public class SimulationSettings {
	private static SimulationSettings simulationSettings;
	private int nTurns;
	private int turnDuration;
	private int curTurn = 0;

    public static SimulationSettings getInstance() {
		if(simulationSettings == null) {
            Properties prop = new Properties();
			String fileName = "app.config";
			try (FileInputStream fis = new FileInputStream(fileName)) {
				prop.load(fis);
			} catch (Exception ex) {
				System.out.println("File app.config not found");
			}
			simulationSettings = new SimulationSettings(prop.getProperty("turn_duration"));
		}
		return simulationSettings;
	}

    private SimulationSettings(String turnDuration){
        int[] tmp = TimeUtils.splitTime(turnDuration);
        int hours = tmp[0];
        int minutes = tmp[1];
        //24:00 = 1440 minutes
		this.turnDuration = hours * 60 + minutes;
        this.nTurns = 1440 / (this.turnDuration);
    }

    

	public static SimulationSettings getSimulationSettings() {
        return simulationSettings;
    }


    public static void setSimulationSettings(SimulationSettings simulationSettings) {
        SimulationSettings.simulationSettings = simulationSettings;
    }


    public int getNTurns() {
        return nTurns;
    }


    public void setNTurns(int nTurns) {
        this.nTurns = nTurns;
    }

    public int getTurnDuration() {
        return turnDuration;
    }

	public int getCurTurn() {
		return curTurn;
	}


   
	


}
