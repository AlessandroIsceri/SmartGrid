package com.ii.smartgrid.smartgrid.behaviours;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;

import jade.core.behaviours.OneShotBehaviour;

public abstract class CustomOneShotBehaviour extends OneShotBehaviour{
    
    protected CustomAgent customAgent;

    public CustomOneShotBehaviour(CustomAgent customAgent){
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