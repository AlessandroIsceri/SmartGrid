package com.ii.smartgrid.behaviours;

import com.ii.smartgrid.agents.CustomAgent;

import jade.core.behaviours.Behaviour;

public abstract class CustomBehaviour extends Behaviour{
    
    protected CustomAgent customAgent;

    protected CustomBehaviour(CustomAgent customAgent){
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
