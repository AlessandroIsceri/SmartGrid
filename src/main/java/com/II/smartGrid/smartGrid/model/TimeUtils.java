package com.II.smartGrid.smartGrid.model;

import java.io.FileInputStream;
import java.util.Properties;

import com.II.smartGrid.smartGrid.tools.SimulationSettings;

public class TimeUtils {
    
	private static int turnDuration = -1;
	
	private static void initTurnDuration() {
		if(turnDuration == -1) {
			Properties prop = new Properties();
			String fileName = "src/main/resources/app.config";
			try (FileInputStream fis = new FileInputStream(fileName)) {
				prop.load(fis);
			} catch (Exception ex) {
				System.out.println("File app.config not found");
			}
			String _turnDuration = prop.getProperty("turn_duration");
			int[] tmp = TimeUtils.splitTime(_turnDuration);
	        int hours = tmp[0];
	        int minutes = tmp[1];
	        //24:00 = 1440 minutes
			turnDuration = hours * 60 + minutes;
		}
	}
	
	public static int getTurnDuration() {
		initTurnDuration();
		return turnDuration;
	}
	
    public static int[] splitTime(String time){
        String[] tmp = time.split(":");
        //returns the array: [hours, minutes]
        return new int[] {Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1])};
    }

	public static int convertTimeToTurn(String time) {
		initTurnDuration();
		int[] tmp = splitTime(time);
        int hours = tmp[0];
        int minutes = tmp[1];
        int timeMinutes = hours * 60 + minutes;
		// 12:30; 00:30 -> 25
        int turn = timeMinutes / turnDuration;
        return turn;
	}
}