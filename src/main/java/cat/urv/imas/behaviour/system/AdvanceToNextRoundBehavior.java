package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.SystemAgent;
import jade.core.behaviours.OneShotBehaviour;

public class AdvanceToNextRoundBehavior extends OneShotBehaviour {

    /**
     *  Milliseconds to wait before advancing to next round.
     */
    private static final int ROUND_TIMEOUT = 100;

    private SystemAgent agent;

    AdvanceToNextRoundBehavior(SystemAgent agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    public void action() {
        // check and apply actions
        agent.advanceToNextRound();
        try {
            Thread.sleep(ROUND_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // send game settings to the agents
        agent.notifyCoordinatorAboutGameStatus();
    }

    @Override
    public int onEnd() {
        if (agent.getGame().hasEnded()) {
            return 1;
        } else return 0;
    }
}
