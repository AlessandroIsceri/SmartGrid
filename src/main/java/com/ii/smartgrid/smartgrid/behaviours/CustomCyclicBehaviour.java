package com.ii.smartgrid.smartgrid.behaviours;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;

import jade.core.behaviours.CyclicBehaviour;

public abstract class CustomCyclicBehaviour extends CyclicBehaviour{
    
    protected CustomAgent customAgent;

    public CustomCyclicBehaviour(CustomAgent customAgent){
        super(customAgent);
        this.customAgent = customAgent;
    }

    @Override
    public void onStart() {
        log("Started");
    }

    @Override
    public int onEnd() {
        log("Finished");
        return super.onEnd();
    }

    public void log(String logMessage){
        customAgent.log(logMessage, this.getBehaviourName());
    }
}
