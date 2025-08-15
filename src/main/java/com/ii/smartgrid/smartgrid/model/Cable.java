package com.ii.smartgrid.smartgrid.model;

import com.ii.smartgrid.smartgrid.utils.TimeUtils;

public class Cable {

    //nel json basta passare cableSection, resistivity, voltage
    private double cableSection;
    private double resistivity;
    private double voltage;

    private double length;
    private double cableResistance;

    private String from;
    private String to;
    private String cableType;
    
    public double computeTransmissionCost(){
        return cableResistance / Math.pow(voltage, 2);
    }

    public String getFrom() {
        return from;
    }


    public void setFrom(String from) {
        this.from = from;
    }


    public String getTo() {
        return to;
    }



    public void setTo(String to) {
        this.to = to;
    }



    public Cable(){
        super();
    }

    public Cable(double cableSection, double resistivity, double voltage, Coordinates firstNodeCoordinates, Coordinates secondNodeCoordinates, String to, String from, String cableType) {
        super();
        this.cableSection = cableSection;
        this.resistivity = resistivity;
        this.voltage = voltage;
        computeAndSetLength(firstNodeCoordinates, secondNodeCoordinates);
        computeAndSetCableResistance();
        this.from = from;
        this.to = to;
        this.cableType = cableType;
    }


    public double getCableSection() {
        return cableSection;
    }


    public void setCableSection(double cableSection) {
        this.cableSection = cableSection;
    }


    public double getLength() {
        return length;
    }


    public void setLength(double length) {
        this.length = length;
    }


    public double getResistivity() {
        return resistivity;
    }


    public void setResistivity(double resistivity) {
        this.resistivity = resistivity;
    }


    public double getVoltage() {
        return voltage;
    }


    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }


    public double getCableResistance() {
        return cableResistance;
    }


    public void setCableResistance(double cableResistance) {
        this.cableResistance = cableResistance;
    }

    private void computeAndSetLength(Coordinates firstNodeCoordinates, Coordinates secondNodeCoordinates){
        double latitudeFirstNodeRadians = firstNodeCoordinates.getRadiansLatitude();
        double longitudeFirstNodeRadians = firstNodeCoordinates.getRadiansLongitude();
        double latitudeSecondNodeRadians = secondNodeCoordinates.getRadiansLatitude();
        double longitudeSecondNodeRadians = secondNodeCoordinates.getRadiansLongitude();

        double earthRadius = 6371000.0;
        double deltaLatitude = latitudeFirstNodeRadians - latitudeSecondNodeRadians;
        double deltaLongitude = longitudeFirstNodeRadians - longitudeSecondNodeRadians;
        double a = Math.pow(Math.sin(deltaLatitude / 2.0), 2) + Math.cos(latitudeFirstNodeRadians) * Math.cos(latitudeSecondNodeRadians) * Math.pow(Math.sin(deltaLongitude / 2.0), 2);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        this.length = earthRadius * c; //metres
    }

    public void computeAndSetCableResistance(){
        this.cableResistance = this.resistivity * this.length / this.cableSection;
    }


    public double computeTransmittedPower(double powerSentWH){
        double powerSent = powerSentWH / TimeUtils.getTurnDurationHours();
        double i = powerSent / voltage;
        double transmittedPower = powerSent - cableResistance *  Math.pow(i, 2);
        double transmittedPowerWH = transmittedPower * TimeUtils.getTurnDurationHours();
        return Math.max(0, transmittedPowerWH);  
    } 

    @Override
    public String toString() {
        return "Cable [cableSection=" + cableSection + ", resistivity=" + resistivity + ", voltage=" + voltage
                + ", length=" + length + ", cableResistance=" + cableResistance + "]";
    }




    public String getCableType() {
        return cableType;
    }




    public void setCableType(String cableType) {
        this.cableType = cableType;
    }

    public double getEnergyToSatifyRequest(double requestedEnergyWH) {
        double requestedEnergy = requestedEnergyWH / TimeUtils.getTurnDurationHours(); //W
           
        // neededEnergy = (+voltage^2 ± √(voltage^4 - 4 * cableResistance * voltage^2*requestedEnergy)) / (2*cableResistance)
        double den = 2.0 * cableResistance;
        double delta = Math.sqrt(Math.pow(voltage, 4) - 4.0 * cableResistance * Math.pow(voltage, 2) * requestedEnergy);
        double sol1 = (Math.pow(voltage, 2) - delta) / den;
        double sol2 = (Math.pow(voltage, 2) + delta) / den;
        double sol1WH = sol1 * TimeUtils.getTurnDurationHours();
        double sol2WH = sol2 * TimeUtils.getTurnDurationHours();
        if(sol1WH > 0 && sol2WH > 0){
            return Math.min(sol1WH, sol2WH); 
        }else if(sol1WH > 0){
            return sol1WH;
        }else if(sol2WH > 0){
            return sol2WH; //WH
        }else{
            return -1;
        }
    }
    
}
