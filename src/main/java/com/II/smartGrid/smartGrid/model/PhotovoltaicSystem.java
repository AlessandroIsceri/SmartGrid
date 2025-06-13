package com.ii.smartgrid.smartgrid.model;

import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WeatherStatus;

public class PhotovoltaicSystem implements EnergyProducer{
	private double hProductionSunny;
	private double hProductionRainy;
	private double hProductionCloudy;
	
	public PhotovoltaicSystem(){
		super();
	}

	public PhotovoltaicSystem(double hProductionSunny, double hProductionRainy, double hProductionCloudy) {
		super();
		this.hProductionSunny = hProductionSunny;
		this.hProductionRainy = hProductionRainy;
		this.hProductionCloudy = hProductionCloudy;
	}

	public double getHProduction(WeatherStatus weather, int hour) {
		//Night time from 20:00  to 6:00
		if(hour < 6 || hour > 19) {
			return 0;
		}
		switch(weather) {
			case SUNNY:
				return hProductionSunny;
			case RAINY:
				return hProductionRainy;
			case CLOUDY:
				return hProductionCloudy;
			default:
				return 0;
		}
	}    

    public double gethProductionSunny() {
        return hProductionSunny;
    }

    public void sethProductionSunny(double hProductionSunny) {
        this.hProductionSunny = hProductionSunny;
    }

    public double gethProductionRainy() {
        return hProductionRainy;
    }

    public void sethProductionRainy(double hProductionRainy) {
        this.hProductionRainy = hProductionRainy;
    }

    public double gethProductionCloudy() {
        return hProductionCloudy;
    }

    public void sethProductionCloudy(double hProductionCloudy) {
        this.hProductionCloudy = hProductionCloudy;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PhotovoltaicSystem other = (PhotovoltaicSystem) obj;
        if (Double.doubleToLongBits(hProductionSunny) != Double.doubleToLongBits(other.hProductionSunny))
            return false;
        if (Double.doubleToLongBits(hProductionRainy) != Double.doubleToLongBits(other.hProductionRainy))
            return false;
        if (Double.doubleToLongBits(hProductionCloudy) != Double.doubleToLongBits(other.hProductionCloudy))
            return false;
        return true;
    }

    @Override
	public String toString() {
		return "PhotovoltaicSystem [hProductionSunny=" + hProductionSunny + ", hProductionRainy=" + hProductionRainy
				+ ", hProductionCloudy=" + hProductionCloudy + "]";
	}
	
}
