package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WeatherStatus;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WindSpeedStatus;

import jade.core.Agent;

public abstract class CustomAgent extends Agent{
	protected int curTurn;
	protected WeatherStatus curWeather;
	protected WindSpeedStatus curWindSpeed;

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

	public WeatherStatus getCurWeather() {
		return curWeather;
	}

	public void setCurWeather(WeatherStatus curWeatherStatus) {
		this.curWeather = curWeatherStatus;
	}

    public WindSpeedStatus getCurWindSpeed() {
        return curWindSpeed;
    }

    public void setCurWindSpeed(WindSpeedStatus curWindSpeed) {
        this.curWindSpeed = curWindSpeed;
    }
	
}
