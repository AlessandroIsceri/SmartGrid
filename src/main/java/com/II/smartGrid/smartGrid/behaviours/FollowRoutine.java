package com.II.smartGrid.smartGrid.behaviours;

import java.util.HashMap;
import java.util.List;

import com.II.smartGrid.smartGrid.agents.SmartHome;
import com.II.smartGrid.smartGrid.model.Appliance;
import com.II.smartGrid.smartGrid.model.Routine;
import com.II.smartGrid.smartGrid.model.Task;
import com.II.smartGrid.smartGrid.model.TimeUtils;
import com.II.smartGrid.smartGrid.tools.SimulationSettings;
import com.II.smartGrid.smartGrid.tools.SimulationSettings.WeatherStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class FollowRoutine extends CyclicBehaviour {	
	
	private ObjectMapper objectMapper = new ObjectMapper();

	
	public FollowRoutine(SmartHome smartHome) {
		super(smartHome);
	}
	
	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchConversationId("turn-" + myAgent.getLocalName());
		ACLMessage receivedMsg = myAgent.receive(mt);
		//owner -> manda msg request -> smartHome riceve, manda agree ed esegue -> inform è andata bene
		if (receivedMsg != null) {
			if(receivedMsg.getPerformative() == ACLMessage.INFORM) {
				List<Appliance> appliances = ((SmartHome) myAgent).getAppliances();	
				Routine routine =  ((SmartHome) myAgent).getRoutine();
				List<Task> tasks = routine.getTasks();
				
				String receivedContent = receivedMsg.getContent();
				TypeReference<HashMap<String, Integer>> typeRef = new TypeReference<HashMap<String, Integer>>() {};
				HashMap<String, Integer> jsonObject;
				try {
					jsonObject = objectMapper.readValue(receivedContent, typeRef);
					int curTurn = jsonObject.get("curTurn");
					((SmartHome) myAgent).setCurTurn(curTurn);
					int weather = jsonObject.get("weather");
					((SmartHome) myAgent).setCurWeatherStatus(WeatherStatus.values()[weather]);
					double expectedConsumption = ((SmartHome) myAgent).getExpectedConsumption();
					int turnDuration = TimeUtils.getTurnDuration();
					for(int i = 0; i < tasks.size(); i++) {
						Task curTask = tasks.get(i);
						int startTurn = TimeUtils.convertTimeToTurn(curTask.getStartTime());
						int endTurn = TimeUtils.convertTimeToTurn(curTask.getEndTime());
						if(startTurn == curTurn) {
							curTask.getAppliance().setOn(true);
							expectedConsumption += curTask.getAppliance().gethConsumption() / turnDuration;
						} else if(endTurn == curTurn) {
							curTask.getAppliance().setOn(false);
							expectedConsumption -= curTask.getAppliance().gethConsumption() / turnDuration;
						}
					}
					((SmartHome) myAgent).setExpectedConsumption(expectedConsumption);
					ACLMessage replyMsg = receivedMsg.createReply(ACLMessage.INFORM);
					myAgent.send(replyMsg);
					((SmartHome) myAgent).log("Tasks and expectedConsumption updated");
					block();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			}
		}else {
			block();
		}
	}
}
