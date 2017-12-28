package cat.urv.imas.onthology;

import jade.content.Concept;

public class MobileAgentAction implements Concept {
    private String actionType;

    public InfoAgent getAgent() {
        return agent;
    }

    public void setAgent(InfoAgent agent) {
        this.agent = agent;
    }

    private InfoAgent agent;

    public MobileAgentAction() {

    }

    public MobileAgentAction(String actionType) {
        this.actionType = actionType;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public MobileAgentAction(String actionType, InfoAgent agent) {
        this.actionType = actionType;
        this.agent = agent;
    }
}
