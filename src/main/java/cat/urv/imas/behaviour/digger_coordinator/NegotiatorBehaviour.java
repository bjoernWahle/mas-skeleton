package cat.urv.imas.behaviour.digger_coordinator;

import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.onthology.DiggerTask;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;

public class NegotiatorBehaviour extends SimpleBehaviour {
    DiggerCoordinatorAgent agent;
    SequentialBehaviour sequentialBehaviour;

    public NegotiatorBehaviour(DiggerCoordinatorAgent agent) {
        super(agent);
        this.agent = agent;

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void action() {
        if(sequentialBehaviour == null) {
            sequentialBehaviour = new SequentialBehaviour();
            TaskContractNetInitiatorBehaviour tcni = null;
            for(DiggerTask task : agent.getTasks()) {
                tcni = new TaskContractNetInitiatorBehaviour(agent, task);
                sequentialBehaviour.addSubBehaviour(tcni);
            }
            agent.addBehaviour(sequentialBehaviour);
        }
    }

    @Override
    public boolean done() {
        if(sequentialBehaviour == null) {
            return false;
        } else {
            return sequentialBehaviour.done();
        }
    }

    @Override
    public int onEnd() {
        // TODO return 1 for coalition building
        return 0;
    }

    @Override
    public void reset() {
        super.reset();
        sequentialBehaviour = null;
    }
}
