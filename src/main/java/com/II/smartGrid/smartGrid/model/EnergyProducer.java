package com.ii.smartgrid.smartgrid.model;

import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WeatherStatus;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type")

@JsonSubTypes({
    @Type(value = PhotovoltaicSystem.class)
})

public interface EnergyProducer {
	public abstract double getHProduction(WeatherStatus weather, int hour);
}

