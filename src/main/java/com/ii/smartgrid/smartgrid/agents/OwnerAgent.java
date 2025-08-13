package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.behaviours.owner.CheckOwnerMessagesBehaviour;
import com.ii.smartgrid.smartgrid.model.Owner;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

public class OwnerAgent extends CustomAgent{
		
    public Owner getOwner(){
        return (Owner) this.referencedObject;
    }

	@Override
    public void setup() {
        String owner = this.getLocalName();
        log(JsonUtil.OWNERS_PATH);
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.OWNERS_PATH, owner, Owner.class);
        
        this.log("Setup completed");
		this.addBehaviour(new CheckOwnerMessagesBehaviour(this));
		
    }
}
