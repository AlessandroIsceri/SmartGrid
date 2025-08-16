package com.ii.smartgrid.smartgrid.behaviours;

import java.util.HashMap;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.Coordinates;
import com.ii.smartgrid.smartgrid.model.CustomObject;
import com.ii.smartgrid.smartgrid.utils.EnergyUtil;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CoordinatesDiscoveryBehaviour extends CustomBehaviour{

    private enum Status {SENDING_MSGS, RECEIVING_MSGS, FINISHED}
    private Status state = Status.SENDING_MSGS;
    private int requestCont = 0;
    private int neighborsCont;

    public CoordinatesDiscoveryBehaviour(CustomAgent customAgent){
        super(customAgent);
        neighborsCont = customAgent.getReferencedObject().getConnectedAgents().size();
    }

    @Override
    public void action() {

        if(neighborsCont == 0){
            this.state = Status.FINISHED;
            return;
        }

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
        // Send coordinates to connected agents
        log("Sending coordinates...");
        CustomObject referencedObject = customAgent.getReferencedObject();
        Map<String, Cable> connectedAgents = referencedObject.getConnectedAgents();
        for(String agentName : connectedAgents.keySet()){
            Map<String, Object> content = new HashMap<>();
            content.put(MessageUtil.OPERATION, MessageUtil.DISCOVERY);
            content.put(MessageUtil.LATITUDE, referencedObject.getCoordinates().getLatitude());
            content.put(MessageUtil.LONGITUDE, referencedObject.getCoordinates().getLongitude());
            customAgent.createAndSend(ACLMessage.INFORM, agentName, content);
        }
        state = Status.RECEIVING_MSGS;
    }

    private void receiveCoordinates(){
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
            String otherAgentName = receivedMsg.getSender().getLocalName();
            String myAgentName = customAgent.getLocalName();      
            log("Received coordinates from " + otherAgentName);
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            String operation = (String) jsonObject.get(MessageUtil.OPERATION);
            if(operation.equals(MessageUtil.DISCOVERY)){
                requestCont++;
                double latitude = (double) jsonObject.get(MessageUtil.LATITUDE);
                double longitude = (double) jsonObject.get(MessageUtil.LONGITUDE);
                Coordinates otherCoordinates = new Coordinates(latitude, longitude);
                Coordinates myCoordinates = customAgent.getReferencedObject().getCoordinates();
                
                Cable cableInfo = EnergyUtil.getCableTypeInfo(myAgentName, otherAgentName);
                double cableSection = cableInfo.getCableSection();
                double resistivity = cableInfo.getResistivity();
                double voltage = cableInfo.getVoltage();
                String from = myAgentName;
                String to = otherAgentName;
                String cableType = cableInfo.getCableType();
                // Initialize the cable with the information received
                Cable cable = new Cable(cableSection, resistivity, voltage, myCoordinates, otherCoordinates, to, from, cableType);
                customAgent.getReferencedObject().addCable(otherAgentName, cable);
            } else {
                log("The received message is not a discovery");
                customAgent.putBack(receivedMsg);
            }

            if(requestCont < neighborsCont){
                customAgent.blockBehaviourIfQueueIsEmpty(this);
            }else{
                sendInformationToLoadManager();
                state = Status.FINISHED;
            }
		} else {
			customAgent.blockBehaviourIfQueueIsEmpty(this);
		}
    }
    
    @Override
    public boolean done() {
        return state == Status.FINISHED;
    }

    protected void sendInformationToLoadManager(){
        // This method will be implemented only by subclasses of CoordinatesDiscoveryBehaviour
    }

}
