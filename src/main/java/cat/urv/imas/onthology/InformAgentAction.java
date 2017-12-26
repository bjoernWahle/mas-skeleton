package cat.urv.imas.onthology;

import jade.content.Predicate;

public class InformAgentAction implements Predicate {
    MobileAgentAction action;

    public InformAgentAction() {

    }

    public InformAgentAction(MobileAgentAction action) {
        this.action = action;
    }

    public MobileAgentAction getAction() {
        return action;
    }

    public void setAction(MobileAgentAction action) {
        this.action = action;
    }
}
