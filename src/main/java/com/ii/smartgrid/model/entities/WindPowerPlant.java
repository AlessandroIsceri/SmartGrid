package com.ii.smartgrid.model.entities;

import com.ii.smartgrid.utils.WeatherUtil;
import com.ii.smartgrid.utils.WeatherUtil.WindState;

public class WindPowerPlant extends RenewablePowerPlant {

    private static final double PRESSURE_COEFFICIENT = 0.4;
    private static final double AIR_DENSITY = 1.225;
    private double minWindSpeed;  // m/s
    private double maxWindSpeed;  // m/s
    private double rotorDiameter; // m
    private double rotorSweptArea; // m^2
    private int numberOfTurbines;

    @Override
    public double getHourlyProduction(Object... parameters) {
        WindState curWindSpeed = (WindState) parameters[0];

        double energyProd;
        double windSpeed;
        windSpeed = WeatherUtil.windSpeedAvg[curWindSpeed.ordinal()];
        // Conversion from km/h to m/s
        windSpeed = windSpeed / 3.6;

        if(windSpeed < minWindSpeed || windSpeed > maxWindSpeed){
            return 0;
        }

        if(windSpeed > (maxWindSpeed + minWindSpeed) / 2){
            windSpeed = (maxWindSpeed + minWindSpeed) / 2;
        }

        rotorSweptArea = (Math.PI * Math.pow((rotorDiameter / 2.0), 2));
        energyProd = 0.5 * AIR_DENSITY * rotorSweptArea * Math.pow(windSpeed, 3) * PRESSURE_COEFFICIENT * numberOfTurbines;
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

    public int getNumberOfTurbines() {
        return numberOfTurbines;
    }

    public void setNumberOfTurbines(int numberOfTurbines) {
        this.numberOfTurbines = numberOfTurbines;
    }

}