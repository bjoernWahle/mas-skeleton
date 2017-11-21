package cat.urv.imas.agent;

import jade.core.AID;

public class DiggerCoordinatorAgent extends ImasAgent {

    /**
     * System agent id.
     */
    private AID systemAgent;

    public DiggerCoordinatorAgent() {
        super(AgentType.DIGGER_COORDINATOR);
    }

    @Override
    public void setup() {
        super.setup();

        this.systemAgent = findSystemAgent();

        // TODO implement and add behaviours
    }

}
