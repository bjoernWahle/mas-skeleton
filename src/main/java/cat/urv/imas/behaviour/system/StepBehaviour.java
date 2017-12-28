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
        ReceiverBehaviour waiting = new ReceiverBehaviour(agent, 30000, MessageTemplate.MatchPerformative(ACLMessage.INFORM)) {
            @Override
            public void onStart() {
                super.onStart();
                agent.log("Waiting for actions and stats.");
            }

            @Override
            public void handle(ACLMessage m) {
                super.handle(m);
                try {
                    ContentElement ce = agent.getContentManager().extractContent(m);
                    if(ce instanceof ActionList) {
                        ActionList diggerActions = (ActionList) ce;
                        // TODO later we should have one message with actions and stats
                        agent.storeActions(diggerActions);
                    }
                } catch (Codec.CodecException e) {
                    e.printStackTrace();
                } catch (OntologyException e) {
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
        agent.log("FSM behaviour completed.");
        agent.doDelete();
        return super.onEnd();
    }
}
