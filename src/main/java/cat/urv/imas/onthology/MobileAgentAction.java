package cat.urv.imas.onthology;

import jade.content.Concept;

public class MobileAgentAction implements Concept {
    String actionType;

    public MobileAgentAction() {

    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public MobileAgentAction(String actionType) {

        this.actionType = actionType;
    }
}
