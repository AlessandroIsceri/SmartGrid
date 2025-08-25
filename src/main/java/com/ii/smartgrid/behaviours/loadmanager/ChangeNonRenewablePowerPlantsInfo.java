package com.ii.smartgrid.behaviours.loadmanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.model.NonRenewablePowerPlantInfo;
import com.ii.smartgrid.model.entities.LoadManager;
import com.ii.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.utils.TimeUtils;

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

        // Turn on non-renewable power plants until the system reaches enough energy
        // Compute the difference between the effective production and the needed energy
        // When the effective production is greater than required energy, turn off the power plants that are no longer required
         
        // The required energy is the amount needed to charge all batteries to 75% if they have less than 25%

        double batteryRequiredEnergy = loadManager.getBatteryRequiredEnergy(loadManagerAgent.getCurTurn());
        double surplusEnergy = loadManager.getSurplusEnergy();
        
        surplusEnergy += (loadManager.getCurTurnRenewableEnergyProduction());

        double nextTurnExpectedConsumption = Math.max(loadManager.getNextTurnExpectedConsumption() - surplusEnergy * 0.7, 0);

        double totalRequiredEnergy = batteryRequiredEnergy + nextTurnExpectedConsumption;
        List<NonRenewablePowerPlantInfo> nonRenewablePowerPlantInfos = loadManager.getNonRenewablePowerPlantInfos();
        double producedEnergy = 0;
        for(int i = 0; i < nonRenewablePowerPlantInfos.size(); i++){
            NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo = nonRenewablePowerPlantInfos.get(i);
            double maxProducibleEnergy = nonRenewablePowerPlantInfo.getMaxTurnProduction();
            double remaining = totalRequiredEnergy - producedEnergy;
            if(remaining > maxProducibleEnergy){
                producedEnergy += nonRenewablePowerPlantInfo.getMaxTurnProduction();
                nonRenewablePowerPlantInfo.setLastTurnProduction(maxProducibleEnergy);
                nonRenewablePowerPlantInfo.setOn(true);
            } else if(remaining > 0.1){
                nonRenewablePowerPlantInfo.setLastTurnProduction(remaining);
                producedEnergy += remaining;
                nonRenewablePowerPlantInfo.setOn(true);
            } else{
                nonRenewablePowerPlantInfo.setOn(false);
            }
        }

        // Send messages containing the information about non-renewable power plants that were activated this turn
        for(NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo : nonRenewablePowerPlantInfos){
            Map<String, Object> content = new HashMap<>();
            content.put(MessageUtil.ON, nonRenewablePowerPlantInfo.isOn());
            content.put(MessageUtil.REQUIRED_ENERGY, nonRenewablePowerPlantInfo.getLastTurnProduction() / TimeUtils.getTurnDurationHours());
            customAgent.createAndSend(ACLMessage.INFORM, nonRenewablePowerPlantInfo.getName(), content);
        }
        

    }

}
