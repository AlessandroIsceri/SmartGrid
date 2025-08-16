package com.ii.smartgrid.smartgrid.model;

import com.ii.smartgrid.smartgrid.utils.WeatherUtil;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WindSpeedStatus;

public class WindPowerPlant extends RenewablePowerPlant {

    private static final double PRESSURE_COEFFICIENT = 0.4;
    private double minWindSpeed;  // m/s
    private double maxWindSpeed;  // m/s
    private double airDensity;    // kg/m3
    private double rotorDiameter; // m
    private double rotorSweptArea; // m^2

    public double getAirDensity() {
        return airDensity;
    }

    public void setAirDensity(double airDensity) {
        this.airDensity = airDensity;
    }

    @Override
    public double getHourlyProduction(Object... weatherConditions) {
        WindSpeedStatus curWindSpeed = (WindSpeedStatus) weatherConditions[0];

        double energyProd;
        double windSpeed;
        windSpeed = WeatherUtil.windSpeedAvg[curWindSpeed.ordinal()];
        // Conversion from km/h to m/s
        windSpeed = windSpeed / 3.6;

        rotorSweptArea = (Math.PI * (rotorDiameter / 2.0));
        energyProd = 0.5 * airDensity * rotorSweptArea * Math.pow(windSpeed, 3) * PRESSURE_COEFFICIENT;
        return energyProd;
    }

    public double getMaxWindSpeed() {
        return maxWindSpeed;
    }

    public void setMaxWindSpeed(double maxWindSpeed) {
        this.maxWindSpeed = maxWindSpeed;
    }

    public double getMinWindSpeed() {
        return minWindSpeed;
    }

    public void setMinWindSpeed(double minWindSpeed) {
        this.minWindSpeed = minWindSpeed;
    }

    public double getRotorDiameter() {
        return rotorDiameter;
    }

    public void setRotorDiameter(double rotorDiameter) {
        this.rotorDiameter = rotorDiameter;
    }

    public double getRotorSweptArea() {
        return rotorSweptArea;
    }

    public void setRotorSweptArea(double rotorSweptArea) {
        this.rotorSweptArea = rotorSweptArea;
    }

}