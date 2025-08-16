package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlantInfo;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.lang.acl.ACLMessage;

public class ChangeNonRenewablePowerPlantsInfo extends CustomOneShotBehaviour{
    
    private LoadManagerAgent loadManagerAgent;

    public ChangeNonRenewablePowerPlantsInfo(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
        this.loadManagerAgent = loadManagerAgent;
    }

    @Override
    public void action() {
        LoadManager loadManager = loadManagerAgent.getLoadManager();

        // Turn on non renewable powerplants until the system reaches enough energy 
        // Compute the difference between the effective production and the needed energy
        // When the effective production is greater than required energy, turn off the powerplants that are no longer required
         
        // The required energy is the amount needed to charge all batteries to 75% if they have less than 25%
        double requiredEnergy = loadManager.getBatteryRequiredEnergy();
        double producedEnergy = 0;
        List<NonRenewablePowerPlantInfo> nonRenewablePowerPlantInfos = loadManager.getNonRenewablePowerPlantInfos();
        int pos = 0;
        for(int i = 0; i < nonRenewablePowerPlantInfos.size(); i++){
            NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo = nonRenewablePowerPlantInfos.get(i);
            if(requiredEnergy > producedEnergy){
                producedEnergy += nonRenewablePowerPlantInfo.getMaxTurnProduction();
                nonRenewablePowerPlantInfo.setOn(true);
                pos = i;
            } else {
                nonRenewablePowerPlantInfo.setOn(false);
            }
        }

        // The first i non renewable powerplants are "on" --> turn off the ones that are not needed 
        for(int j = pos - 1; j > 0; j--){
            NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo = nonRenewablePowerPlantInfos.get(j);
            double ppMaxTurnProduction = nonRenewablePowerPlantInfo.getMaxTurnProduction();
            if(producedEnergy - ppMaxTurnProduction > requiredEnergy){
                producedEnergy -= ppMaxTurnProduction;
                nonRenewablePowerPlantInfo.setOn(false);
            }
        }

        // Send messages containing the information about non renewable powerplants that were activated this turn
        for(NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo : nonRenewablePowerPlantInfos){
            Map<String, Object> content = new HashMap<>();
            content.put(MessageUtil.ON, nonRenewablePowerPlantInfo.isOn());
            customAgent.createAndSend(ACLMessage.INFORM, nonRenewablePowerPlantInfo.getName(), content);
        }
        
    }

}
