package com.ii.smartgrid.smartgrid.behaviours;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.Coordinates;
import com.ii.smartgrid.smartgrid.model.CustomObject;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.utils.EnergyUtil;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CoordinatesDiscoveryBehaviour extends Behaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    private enum Status {SENDING_MSGS, RECEIVING_MSGS, FINISHED}
    private Status state = Status.SENDING_MSGS;
    private int requestCont = 0;
    private int neighborsCont;

    public CoordinatesDiscoveryBehaviour(CustomAgent customAgent){
        super(customAgent);
        neighborsCont = ((CustomAgent) myAgent).getReferencedObject().getConnectedAgents().size();
    }

    @Override
    public void action() {

        if(neighborsCont == 0){
            this.state = Status.FINISHED;
            
            return;
        }

        //send messages
        switch (state) {
            case SENDING_MSGS:
                sendCoordinates();
                break;
            case RECEIVING_MSGS:
                receiveCoordinates();
                break;
            default:
                break;
        }
    }

    private void sendCoordinates(){
        ((CustomAgent) myAgent).log("Sending coordinates...", BEHAVIOUR_NAME);
        CustomObject referencedObject = ((CustomAgent) myAgent).getReferencedObject();
        Map<String, Cable> connectedAgents = referencedObject.getConnectedAgents();
        for(String agentName : connectedAgents.keySet()){
            Map<String, Object> content = new HashMap<String, Object>();
            content.put(MessageUtil.OPERATION, MessageUtil.DISCOVERY);
            content.put(MessageUtil.LATITUDE, referencedObject.getCoordinates().getLatitude());
            content.put(MessageUtil.LONGITUDE, referencedObject.getCoordinates().getLongitude());
            ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, agentName, content);
        }
        state = Status.RECEIVING_MSGS;
    }

    private void receiveCoordinates(){
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            String otherAgentName = receivedMsg.getSender().getLocalName();
            String myAgentName = myAgent.getLocalName();      
            ((CustomAgent) myAgent).log("Received coordinates from " + otherAgentName, BEHAVIOUR_NAME);
            Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
            String operation = (String) jsonObject.get(MessageUtil.OPERATION);
            if(operation.equals(MessageUtil.DISCOVERY)){
                requestCont++;
                double latitude = (double) jsonObject.get(MessageUtil.LATITUDE);
                double longitude = (double) jsonObject.get(MessageUtil.LONGITUDE);
                Coordinates otherCoordinates = new Coordinates(latitude, longitude);
                Coordinates myCoordinates = ((CustomAgent) myAgent).getReferencedObject().getCoordinates();
                
                Cable cableInfo = EnergyUtil.getCableTypeInfo(myAgentName, otherAgentName);
                double cableSection = cableInfo.getCableSection();
                double resistivity = cableInfo.getResistivity();
                double voltage = cableInfo.getVoltage();
                // String to = cableInfo.getTo();
                // String from = cableInfo.getFrom();
                String from = myAgentName;
                String to = otherAgentName;
                String cableType = cableInfo.getCableType();
                Cable cable = new Cable(cableSection, resistivity, voltage, myCoordinates, otherCoordinates, to, from, cableType);

                ((CustomAgent) myAgent).getReferencedObject().addCable(otherAgentName, cable);
            } else {
                ((CustomAgent) myAgent).log("The received message is not a discovery", BEHAVIOUR_NAME);
                myAgent.putBack(receivedMsg);
            }

            if(requestCont < neighborsCont){
                ((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
            }else{
                ((CustomAgent) myAgent).log("Coordinates discovery done", BEHAVIOUR_NAME);
                
                sendInformationToLoadManager();
                
                state = Status.FINISHED;
                ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);
            }
		} else {
			((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
		}
    }
    
    @Override
    public boolean done() {
        return state == Status.FINISHED;
    }

    protected void sendInformationToLoadManager(){
        
    }

}
