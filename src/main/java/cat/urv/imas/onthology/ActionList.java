package cat.urv.imas.onthology;

import jade.content.Predicate;

import java.util.List;

public class ActionList implements Predicate {
    List<MobileAgentAction> agentActions;

    public ActionList() {

    }

    public ActionList(List<MobileAgentAction> agentActions) {
        this.agentActions = agentActions;
    }

    public List<MobileAgentAction> getAgentActions() {
        return agentActions;
    }

    public void setAgentActions(List<MobileAgentAction> agentActions) {
        this.agentActions = agentActions;
    }
    
    public void addAgentActions(List<MobileAgentAction> agentActions) {
        this.agentActions.addAll(agentActions);
    }
}
