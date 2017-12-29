package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.SystemAgent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;

public class AdvanceToNextRoundBehavior extends OneShotBehaviour {
    SystemAgent agent;
    public AdvanceToNextRoundBehavior(SystemAgent agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    public void onStart() {
        super.onStart();
        agent.log("AdvanceToNextRound behaviour started.");
    }

    @Override
    public void action() {
        // check and apply actions
        agent.advanceToNextRound();
        // send game settings to the agents
        agent.notifyCoordinator();
    }

    @Override
    public int onEnd() {
        if (agent.getGame().hasEnded()) {
            return 1;
        } else return 0;
    }
}
