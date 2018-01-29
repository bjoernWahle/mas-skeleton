package cat.urv.imas.onthology;

import jade.content.Predicate;

import java.util.ArrayList;
import java.util.List;

public class ActionList implements Predicate {
    List<MobileAgentAction> agentActions;

    public ActionList() {

    }

    public ActionList(List<MobileAgentAction> agentActions) {
    	if(agentActions == null) {
    		this.agentActions = new ArrayList<MobileAgentAction>();
    	}else {
    		this.agentActions = agentActions;
    	}
    }

    public List<MobileAgentAction> getAgentActions() {
        return agentActions;
    }

    public void setAgentActions(List<MobileAgentAction> agentActions) {
        this.agentActions = agentActions;
    }
    
    public void addAgentActions(List<MobileAgentAction> agentActions) {
    	if(agentActions != null) {
    		if(!agentActions.isEmpty())
    		this.agentActions.addAll(agentActions);
    	}
    }
}
