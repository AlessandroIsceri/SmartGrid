package com.ii.smartgrid.smartgrid.model;

public class Cable {

    //nel json basta passare cableSection, resistivity, voltage
    private double cableSection;
    private double resistivity;
    private double voltage;

    private double length;
    private double cableResistance;
    
    public Cable(){
        super();
    }

    public Cable(double cableSection, double resistivity, double voltage, Coordinates firstNodeCoordinates, Coordinates secondNodeCoordinates) {
        super();
        this.cableSection = cableSection;
        this.resistivity = resistivity;
        this.voltage = voltage;
        computeAndSetLength(firstNodeCoordinates, secondNodeCoordinates);
        computeAndSetCableResistance();
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
        double latitudeFirstNodeRadiants = firstNodeCoordinates.getRadiantLatitude();
        double longitudeFirstNodeRadiants = firstNodeCoordinates.getRadiantLongitude();
        double latitudeSecondNodeRadiants = secondNodeCoordinates.getRadiantLatitude();
        double longitudeSecondNodeRadiants = secondNodeCoordinates.getRadiantLongitude();

        double earthRadius = 6378;
        double deltaLatitude = latitudeFirstNodeRadiants - latitudeSecondNodeRadiants;
        double deltaLongitude = longitudeFirstNodeRadiants - longitudeSecondNodeRadiants;
        double a = Math.pow(Math.sin(deltaLatitude / 2), 2) + Math.cos(latitudeFirstNodeRadiants) * Math.cos(latitudeSecondNodeRadiants) * Math.pow(Math.sin(deltaLongitude / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        this.length = earthRadius * c;
    }

    public void computeAndSetCableResistance(){
        this.cableResistance = this.resistivity * this.cableSection / this.length;
    }

    public double computeTransmittedPower(double powerSent){
        return Math.max(0, powerSent - Math.pow(voltage, 2) / cableResistance);  
    }

    @Override
    public String toString() {
        return "Cable [cableSection=" + cableSection + ", resistivity=" + resistivity + ", voltage=" + voltage
                + ", length=" + length + ", cableResistance=" + cableResistance + "]";
    }
    
}
