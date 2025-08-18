package com.ii.smartgrid.agents;

import com.ii.smartgrid.behaviours.owner.CheckOwnerMessagesBehaviour;
import com.ii.smartgrid.model.entities.Owner;
import com.ii.smartgrid.utils.JsonUtil;

public class OwnerAgent extends CustomAgent{
		
    public Owner getOwner(){
        return (Owner) this.referencedObject;
    }

	@Override
    public void setup() {
        String owner = this.getLocalName();
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.OWNERS_PATH, owner, Owner.class);
        
		this.addBehaviour(new CheckOwnerMessagesBehaviour(this));
        this.log("Setup completed");
    }
}
