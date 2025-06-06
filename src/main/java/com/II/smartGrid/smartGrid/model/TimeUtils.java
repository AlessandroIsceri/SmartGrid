package com.II.smartGrid.smartGrid.model;

import com.II.smartGrid.smartGrid.tools.SimulationSettings;

public class TimeUtils {
    
    private static SimulationSettings simulationSettings = SimulationSettings.getInstance();

    public static int[] splitTime(String time){
        String[] tmp = time.split(":");
        //returns the array: [hours, minutes]
        return new int[] {Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1])};
    }

	public static int convertTimeToTurn(String time) {
        int turnDuration = simulationSettings.getTurnDuration();
		int[] tmp = splitTime(time);
        int hours = tmp[0];
        int minutes = tmp[1];
        int timeMinutes = hours * 60 + minutes;
		// 12:30; 00:30 -> 25
        int turn = timeMinutes / turnDuration;
        return turn;
	}
}