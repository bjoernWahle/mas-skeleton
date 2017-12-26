package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.CoordinatorAgent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

public class CoordinatorBehaviour extends FSMBehaviour {
    CoordinatorAgent agent;

    public CoordinatorBehaviour(CoordinatorAgent coordinatorAgent) {
        this.agent = coordinatorAgent;
    }
}
