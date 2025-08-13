package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlantInfo;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class ChangeNonRenewablePowerPlantsInfo extends CustomOneShotBehaviour{
    
    public ChangeNonRenewablePowerPlantsInfo(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
    }

    @Override
    public void action() {
        LoadManager loadManager = ((LoadManagerAgent) myAgent).getLoadManager();
        double requestedEnergySum = loadManager.getAllRequestedEnergySum();
    
        if(requestedEnergySum > 0){
            //No requests to be satisfied
            return;
        }

        /*
         * if(almeno una batteria è < 25) -> attiva non rinnovabili (quante e quali?)
         * elif(tutte sopra > 75) -> disattivano TUTTE non rinnovabili
         */
        
        // 250; 500; 750; 1000; 2000; da produrre = 800 -> 250+500+750 = 1500 - 800 = 700 > 500 -> 250+750 -> 1000 - 800 = 200
        // 900 -> 250 + 500 + 750 = 1500 - 900 = 600 -> 250 + 750 = 1000 - 900 = 100
        // 1001 -> 250 + 500 + 750 + 1000 = 2000 - 1001 = 999 -> 999-750-500

        // accendo le pp finchè non supero l'energia da produrre
        // calcolo la differenza tra ciò che produco e ciò che serve produrre
        // finche la differenza è > 0, spengo a partire da destra (esclusa l'ultima)

        // contiamo quante sono < 25% e facciamo la somma di ciò che gli serve per tornare al 75% -> 100.000
        // attivare e disattivare pp non rinnovabili di conseguenza

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

        //the first i non renewable powerplants are "on".. check if are all needed 
        for(int j = pos - 1; j > 0; j--){
            NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo = nonRenewablePowerPlantInfos.get(j);
            double ppMaxTurnProduction = nonRenewablePowerPlantInfo.getMaxTurnProduction();
            if(producedEnergy - ppMaxTurnProduction > requiredEnergy){
                producedEnergy -= ppMaxTurnProduction;
                nonRenewablePowerPlantInfo.setOn(false);
            }
        }

        for(NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo : nonRenewablePowerPlantInfos){
            Map<String, Object> content = new HashMap<String, Object>();
            content.put(MessageUtil.ON, nonRenewablePowerPlantInfo.isOn());
            ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, nonRenewablePowerPlantInfo.getName(), content);
        }
        
    }

}
