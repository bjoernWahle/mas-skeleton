package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.behaviour.ReceiverBehaviour;
import cat.urv.imas.onthology.ActionList;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
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
        ReceiverBehaviour waiting = new ReceiverBehaviour(agent, MessageTemplate.MatchPerformative(ACLMessage.INFORM), true) {
            @Override
            public void onStart() {
                super.onStart();
                agent.log("Starting round "+agent.getGame().getCurrentSimulationStep());
            }

            @Override
            public void handle(ACLMessage m) {
                super.handle(m);
                try {
                    ContentElement ce = agent.getContentManager().extractContent(m);
                    if(ce instanceof ActionList) {
                        ActionList agentActions = (ActionList) ce;
                        agent.storeActions(agentActions);
                    }
                } catch (Codec.CodecException | OntologyException e) {
                    e.printStackTrace();
                }
            }
        };
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        registerFirstState(new RequestResponseBehaviour(agent, mt), INIT);
        registerState(waiting, WAITING);
        registerState(new AdvanceToNextRoundBehavior(agent), ADVANCING);
        registerLastState(new GameEndedBehaviour(agent), END);

        registerDefaultTransition(INIT, WAITING);
        registerDefaultTransition(WAITING, ADVANCING);
        registerTransition(ADVANCING, WAITING, 0);
        registerTransition(ADVANCING, END, 1);

    }

    public int onEnd() {
        agent.doDelete();
        return super.onEnd();
    }
}
