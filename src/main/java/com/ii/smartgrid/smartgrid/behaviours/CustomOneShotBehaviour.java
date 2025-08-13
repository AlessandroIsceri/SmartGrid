package com.ii.smartgrid.smartgrid.behaviours;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;

import jade.core.behaviours.OneShotBehaviour;

public abstract class CustomOneShotBehaviour extends OneShotBehaviour{
    
  	protected final String BEHAVIOUR_NAME = this.getClass().getSimpleName();
    
    public CustomOneShotBehaviour(CustomAgent customAgent){
        super(customAgent);
    }

    @Override
    public void onStart() {
        ((CustomAgent) myAgent).log("Started", BEHAVIOUR_NAME);
    }

    @Override
    public int onEnd() {
        ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);
        return super.onEnd();
    }
}