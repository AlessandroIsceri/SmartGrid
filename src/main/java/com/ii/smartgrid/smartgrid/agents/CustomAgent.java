package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WeatherStatus;

import jade.core.Agent;

public abstract class CustomAgent extends Agent{
	protected int curTurn;
	protected WeatherStatus curWeatherStatus;
	
	public void log(String message){
		//1 - day 1 - 00:15 - HOME1 --> messaggio
		//24:00 = 1440 minutes
		int turnDuration = TimeUtils.getTurnDuration();
		int day = ((curTurn * turnDuration) / 1440) + 1;
		int curDayTurn = curTurn % (1440 / turnDuration);
		System.out.println(this.curTurn + " - " + "Day " + day + " - " + TimeUtils.convertTurnToTime(curDayTurn) + " - " + this.getLocalName() + " --> " + message);
	}
	
	public int getCurTurn() {
		return curTurn;
	}
	
	public void setCurTurn(int curTurn) {
		this.curTurn = curTurn;
	}

	public WeatherStatus getCurWeatherStatus() {
		return curWeatherStatus;
	}

	public void setCurWeatherStatus(WeatherStatus curWeatherStatus) {
		this.curWeatherStatus = curWeatherStatus;
	}
	
}
