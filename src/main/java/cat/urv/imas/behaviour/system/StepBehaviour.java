package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.SystemAgent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class StepBehaviour extends FSMBehaviour {
    private SystemAgent agent;

    private static final String INIT = "INIT";
    private static final String ADVANCING = "ADVANCING";
    private static final String WAITING = "WAITING";
    private static final String END = "END";

    public StepBehaviour(SystemAgent systemAgent) {
        agent = systemAgent;
        // add behaviours
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        registerFirstState(new RequestResponseBehaviour(agent, mt), INIT);
        registerState(new HandleActionsBehaviour(agent), WAITING);
        registerState(new AdvanceToNextRoundBehavior(agent), ADVANCING);
        registerLastState(new GameEndedBehaviour(agent), END);

        registerDefaultTransition(INIT, WAITING);
        registerDefaultTransition(WAITING, ADVANCING);
        registerTransition(ADVANCING, WAITING, 0);
        registerTransition(ADVANCING, END, 1);

    }

    public int onEnd() {
        agent.log("FSM behaviour completed.");
        agent.doDelete();
        return super.onEnd();
    }
}
