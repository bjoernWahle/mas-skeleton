package cat.urv.imas.behaviour.digger_coordinator;

import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.onthology.DiggerTask;
import cat.urv.imas.onthology.TaskState;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;

public class NegotiatorBehaviour extends SimpleBehaviour {
    private DiggerCoordinatorAgent agent;
    private SequentialBehaviour sequentialBehaviour;

    NegotiatorBehaviour(DiggerCoordinatorAgent agent) {
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
            int i = 0;
            for(DiggerTask task : agent.getNotStartedTasks()) {
                boolean last = false;
                if(i++ == agent.getNotStartedTasks().size()-1) {
                    last = true;
                }
                TaskContractNetInitiatorBehaviour tcni = new TaskContractNetInitiatorBehaviour(agent, task,last);
                sequentialBehaviour.addSubBehaviour(tcni);
            }
            agent.addBehaviour(sequentialBehaviour);
        }
    }

    @Override
    public boolean done() {
        return sequentialBehaviour != null && sequentialBehaviour.done();
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
