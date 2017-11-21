package cat.urv.imas.agent;

import jade.core.AID;

public class ProspectorAgent extends ImasAgent implements MovingAgentInterface {

    /**
     * System agent id.
     */
    private AID systemAgent;

    public ProspectorAgent() {
        super(AgentType.PROSPECTOR);
    }

    @Override
    public void setup() {
        super.setup();

        this.systemAgent = findSystemAgent();

        // TODO implement and add behaviours
    }

    @Override
    public int stepsToPosition(int row, int col) {
        // TODO implement method
        return 0;
    }
}
