package cat.urv.imas.agent;

import jade.core.AID;

public class ProspectorCoordinatorAgent extends ImasAgent {

    /**
     * System agent id.
     */
    private AID systemAgent;

    public ProspectorCoordinatorAgent() {
        super(AgentType.PROSPECTOR_COORDINATOR);
    }

    @Override
    public void setup() {
        super.setup();

        this.systemAgent = findSystemAgent();

        // TODO implement and add behaviours
    }
}
